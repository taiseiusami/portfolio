package com.amoibeojt.api.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amoibeojt.api.dto.PartsReceivingUpdateDTO;
import com.amoibeojt.api.entity.PartsStock;
import com.amoibeojt.api.entity.PartsStockHistory;
import com.amoibeojt.api.repository.PartsReceivingUpdateRepository;

import lombok.RequiredArgsConstructor;

/**
 * 部品入荷登録サービス（外部指定IDをそのまま利用）
 */
@Service
@RequiredArgsConstructor
public class PartsReceivingUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartsReceivingUpdateService.class);
    private final PartsReceivingUpdateRepository repository;

    @Transactional
    public void register(PartsReceivingUpdateDTO dto) {
        String formattedTransactionDate = convertToMysqlDatetime(dto.getTransactionDate());

        dto.getItems().forEach(item -> {
            boolean updated = false;
            int retryCount = 0;

            while (!updated && retryCount < 3) {
                try {
                    // 必須チェック
                    if (item.getStockId() == null || item.getStockId().isBlank()) {
                        throw new IllegalArgumentException("stockIdIsRequired");
                    }
                    if (item.getCenterId() == null || item.getCenterId().isBlank()) {
                        throw new IllegalArgumentException("centerIdIsRequired");
                    }
                    if (item.getCategoryId() == null || item.getCategoryId().isBlank()) {
                        throw new IllegalArgumentException("categoryIdIsRequired");
                    }
                    if (item.getAmountReceive() == null || item.getAmountReceive().isBlank()) {
                        throw new IllegalArgumentException("amountReceiveIsRequired");
                    }

                    int stockId = Integer.parseInt(item.getStockId());
                    int centerId = Integer.parseInt(item.getCenterId());
                    int categoryId = Integer.parseInt(item.getCategoryId());
                    int amountReceive = Integer.parseInt(item.getAmountReceive());

                    PartsStock stock = repository.findByStockId(stockId);
                    Integer amountBefore = (stock != null) ? stock.getAmount() : 0;

                    String remarks;
                    if (stock != null) {
                        // ★ DTOのversionとDBのversionを比較
                        if (item.getVersion() != null && !item.getVersion().isBlank()) {
                            int clientVersion = Integer.parseInt(item.getVersion());
                            if (!stock.getVersion().equals(clientVersion)) {
                                throw new OptimisticLockingFailureException(
                                        "在庫更新競合: DB version=" + stock.getVersion() + ", client version=" + clientVersion);
                            }
                        }

                        // 更新処理
                        stock.setAmount(amountBefore + amountReceive);
                        stock.setName(item.getPartsName());
                        stock.setCenterId(centerId);
                        stock.setCategoryId(categoryId);
                        stock.setDescription(item.getDescription());
                        stock.setUpdateDate(LocalDateTime.now());

                        remarks = "在庫調整";
                    } else {
                        // 新規登録
                        stock = new PartsStock();
                        stock.setStockId(stockId);
                        stock.setAmount(amountReceive);
                        stock.setName(item.getPartsName());
                        stock.setCenterId(centerId);
                        stock.setCategoryId(categoryId);
                        stock.setDescription(item.getDescription());
                        stock.setDeleteFlag(false);
                        stock.setCreateDate(LocalDateTime.now());
                        stock.setUpdateDate(LocalDateTime.now());
                        stock.setVersion(0); // 新規は0で初期化
                        remarks = "初回入荷";
                    }

                    // 保存
                    stock = repository.saveStock(stock);

                    // 履歴登録
                    PartsStockHistory history = new PartsStockHistory();
                    history.setStockId(stock.getStockId());
                    history.setTransactionType(dto.getTransactionType());
                    history.setTransactionDate(LocalDateTime.parse(formattedTransactionDate,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    history.setAmountBefore(amountBefore);
                    history.setAmountChange(amountReceive);
                    history.setAmountAfter(amountBefore + amountReceive);
                    history.setSupplierName(dto.getSupplierName());
                    history.setPurchaseOrderNo(dto.getPurchaseOrderNo());
                    history.setOperatorName(dto.getOperatorName());
                    history.setRemarks(remarks);
                    history.setDeleteFlag(false);
                    history.setCreateDate(LocalDateTime.now());
                    history.setUpdateDate(LocalDateTime.now());

                    repository.saveHistory(history);
                    LOGGER.info("履歴登録完了 stockId={}", stock.getStockId());
                    updated = true;

                } catch (OptimisticLockingFailureException e) {
                    retryCount++;
                    LOGGER.warn("在庫更新競合。リトライ {}/3", retryCount);
                    if (retryCount >= 3) {
                        throw e;
                    }
                }
            }
        });
    }

    private String convertToMysqlDatetime(String isoDateTime) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(isoDateTime);
            return odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new IllegalArgumentException("transactionDateInvalidFormat");
        }
    }
}