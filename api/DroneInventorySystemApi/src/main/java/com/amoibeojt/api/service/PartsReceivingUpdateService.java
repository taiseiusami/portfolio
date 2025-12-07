package com.amoibeojt.api.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amoibeojt.api.dto.PartsReceivingUpdateDTO;
import com.amoibeojt.api.entity.PartsStock;
import com.amoibeojt.api.entity.PartsStockHistory;
import com.amoibeojt.api.repository.PartsReceivingUpdateRepository;

import lombok.RequiredArgsConstructor;

/**
 * 部品入荷登録サービス
 */
@Service
@RequiredArgsConstructor
public class PartsReceivingUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartsReceivingUpdateService.class);

    private final PartsReceivingUpdateRepository repository;

    @Transactional
    public void register(PartsReceivingUpdateDTO dto) {
        try {
            String formattedTransactionDate = convertToMysqlDatetime(dto.getTransactionDate());

            List<Integer> stockIds = dto.getItems().stream()
                    .map(item -> Integer.parseInt(item.getStockId()))
                    .collect(Collectors.toList());

            Map<Integer, PartsStock> currentStocks = repository.findByStockIds(stockIds)
                    .stream()
                    .collect(Collectors.toMap(PartsStock::getStockId, s -> s));

            dto.getItems().forEach(item -> {
                int stockId = Integer.parseInt(item.getStockId());
                int centerId = Integer.parseInt(item.getCenterId());
                int categoryId = Integer.parseInt(item.getCategoryId());
                int amountReceive = Integer.parseInt(item.getAmountReceive());

                PartsStock stock = currentStocks.get(stockId);
                Integer amountBefore = (stock != null) ? stock.getAmount() : 0;

                String remarks;
                if (stock != null) {
                    LOGGER.info("在庫ID {} が存在するため更新処理を実施", stockId);
                    stock.setAmount(amountBefore + amountReceive);
                    stock.setName(item.getPartsName());
                    stock.setCenterId(centerId);
                    stock.setCategoryId(categoryId);
                    stock.setDescription(item.getDescription());
                    stock.setUpdateDate(java.time.LocalDateTime.now());
                    repository.saveStock(stock);
                    remarks = "在庫調整";
                } else {
                    LOGGER.info("在庫ID {} が存在しないため新規作成を実施", stockId);
                    stock = new PartsStock();
                    stock.setStockId(stockId);
                    stock.setAmount(amountReceive);
                    stock.setName(item.getPartsName());
                    stock.setCenterId(centerId);
                    stock.setCategoryId(categoryId);
                    stock.setDescription(item.getDescription());
                    stock.setDeleteFlag(false);
                    stock.setCreateDate(java.time.LocalDateTime.now());
                    stock.setUpdateDate(java.time.LocalDateTime.now());
                    repository.saveStock(stock);
                    remarks = "初回入荷";
                }

                Integer amountAfter = amountBefore + amountReceive;

                PartsStockHistory history = new PartsStockHistory();
                history.setStockId(stockId);
                history.setTransactionType(dto.getTransactionType());
                history.setTransactionDate(java.time.LocalDateTime.parse(formattedTransactionDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                history.setAmountBefore(amountBefore);
                history.setAmountChange(amountReceive);
                history.setAmountAfter(amountAfter);
                history.setSupplierName(dto.getSupplierName());
                history.setPurchaseOrderNo(dto.getPurchaseOrderNo());
                history.setOperatorName(dto.getOperatorName());
                history.setRemarks(remarks);
                history.setDeleteFlag(false);
                history.setCreateDate(java.time.LocalDateTime.now());
                history.setUpdateDate(java.time.LocalDateTime.now());

                repository.saveHistory(history);

                LOGGER.info("部品入出庫履歴を登録しました stockId={}", stockId);
            });
        } catch (Exception e) {
            LOGGER.error("部品入荷処理中にエラーが発生しました", e);
            throw e;
        }
    }

    /**
     * ISO 8601 → MySQL DATETIME 形式へ変換
     * 例: "2024-12-15T14:30:00Z" → "2024-12-15 14:30:00"
     */
    private String convertToMysqlDatetime(String isoDateTime) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(isoDateTime);
            return odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new IllegalArgumentException("transaction_dateの形式が不正です。");
        }
    }
}