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
 * 部品入荷登録サービス（楽観ロック対応＋リトライ）
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
                    int stockId = Integer.parseInt(item.getStockId());
                    int centerId = Integer.parseInt(item.getCenterId());
                    int categoryId = Integer.parseInt(item.getCategoryId());
                    int amountReceive = Integer.parseInt(item.getAmountReceive());

                    PartsStock stock = repository.findByStockId(stockId);
                    Integer amountBefore = (stock != null) ? stock.getAmount() : 0;

                    String remarks;
                    if (stock != null) {
                        stock.setAmount(amountBefore + amountReceive);
                        stock.setName(item.getPartsName());
                        stock.setCenterId(centerId);
                        stock.setCategoryId(categoryId);
                        stock.setDescription(item.getDescription());
                        stock.setUpdateDate(LocalDateTime.now());
                        remarks = "在庫調整";
                    } else {
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
                        remarks = "初回入荷";
                    }

                    repository.saveStock(stock); // 楽観ロックが効く

                    PartsStockHistory history = new PartsStockHistory();
                    history.setStockId(stockId);
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
                    LOGGER.info("履歴登録完了 stockId={}", stockId);
                    updated = true;

                } catch (OptimisticLockingFailureException e) {
                    retryCount++;
                    LOGGER.warn("在庫ID {} の更新競合。リトライ {}/3", item.getStockId(), retryCount);
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
            throw new IllegalArgumentException("transaction_dateの形式が不正です。");
        }
    }
}