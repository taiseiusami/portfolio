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
import com.amoibeojt.api.repository.PartsReceivingUpdateRepository;

import lombok.RequiredArgsConstructor;

/**
 * 部品入荷登録サービス
 * 在庫テーブル(parts_stock)更新／新規作成＋履歴テーブル(parts_stock_history)登録
 */
@Service
@RequiredArgsConstructor
public class PartsReceivingUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartsReceivingUpdateService.class);

    private final PartsReceivingUpdateRepository partsReceivingUpdateRepository;

    @Transactional
    public void register(PartsReceivingUpdateDTO dto) {
        try {
            // ISO 8601 → MySQL DATETIME 形式へ変換
            String formattedTransactionDate = convertToMysqlDatetime(dto.getTransactionDate());

            // 事前に全 stockId を抽出
            List<Integer> stockIds = dto.getItems().stream()
                    .map(item -> Integer.parseInt(item.getStockId()))
                    .collect(Collectors.toList());

            // Repository に一括取得メソッドを追加して呼び出し
            Map<Integer, Integer> currentAmounts = partsReceivingUpdateRepository.getAmountsByStockIds(stockIds);

            dto.getItems().forEach(item -> {
                // String → int に変換（Validatorでチェック済みなので安全）
                int stockId = Integer.parseInt(item.getStockId());
                int centerId = Integer.parseInt(item.getCenterId());
                int categoryId = Integer.parseInt(item.getCategoryId());
                int amountReceive = Integer.parseInt(item.getAmountReceive());

                // 在庫レコード存在判定（Mapを参照）
                boolean exists = currentAmounts.containsKey(stockId);

                // 更新前数量を取得（存在しなければ0）
                Integer amountBefore = currentAmounts.getOrDefault(stockId, 0);

                String remarks;
                if (exists) {
                    // 在庫更新処理
                    LOGGER.info("在庫ID {} が存在するため更新処理を実施", stockId);
                    partsReceivingUpdateRepository.updateStock(
                        stockId,
                        amountReceive,
                        item.getPartsName(),
                        centerId,
                        categoryId,
                        item.getDescription()
                    );
                    remarks = "在庫調整";
                } else {
                    // 在庫新規作成処理
                    LOGGER.info("在庫ID {} が存在しないため新規作成を実施", stockId);
                    partsReceivingUpdateRepository.insertStock(
                        stockId,
                        amountReceive,
                        item.getPartsName(),
                        centerId,
                        categoryId,
                        item.getDescription()
                    );
                    remarks = "初回入荷";
                }

                // 更新後数量を計算
                Integer amountAfter = amountBefore + amountReceive;

                // 履歴登録処理
                partsReceivingUpdateRepository.insertHistory(
                    stockId,
                    dto.getTransactionType(),
                    formattedTransactionDate,
                    dto.getSupplierName(),
                    dto.getPurchaseOrderNo(),
                    dto.getOperatorName(),
                    amountReceive,
                    remarks,
                    amountBefore,
                    amountAfter
                );

                LOGGER.info("部品入出庫履歴を登録しました stockId={}", stockId);
            });
        } catch (Exception e) {
            LOGGER.error("部品入荷処理中にエラーが発生しました", e);
            throw e; // Controller側で500を返す
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