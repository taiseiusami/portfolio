package com.amoibeojt.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * 部品在庫更新リポジトリ
 * Entity を使わずに SQL 直接実行
 */
@Repository
@RequiredArgsConstructor
public class PartsReceivingUpdateRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 在庫ID存在チェック
     * @param stockId 在庫ID
     * @return true: 存在する, false: 存在しない
     */
    public boolean existsByStockId(Integer stockId) {
        String sql = "SELECT COUNT(*) FROM parts_stock WHERE stock_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, stockId);
        return count != null && count > 0;
    }

    /**
     * 現在数量取得
     * @param stockId 在庫ID
     * @return 現在数量
     */
    public Integer getCurrentAmount(Integer stockId) {
        String sql = "SELECT amount FROM parts_stock WHERE stock_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, stockId);
    }

    /**
     * 部品在庫テーブル更新（既存カラムのみ）
     * create_date は保持、update_date を更新
     */
    public void updateStock(Integer stockId,
                            Integer receiveAmount,
                            String partsName,
                            Integer centerId,
                            Integer categoryId,
                            String description) {
        String sql = "UPDATE parts_stock SET amount = amount + ?, name = ?, center_id = ?, category_id = ?, description = ?, update_date = NOW() " +
                     "WHERE stock_id = ?";
        jdbcTemplate.update(sql, receiveAmount, partsName, centerId, categoryId, description, stockId);
    }

    /**
     * 部品在庫テーブル新規作成（既存カラムのみ）
     * create_date と update_date を両方 NOW() でセット
     */
    public void insertStock(Integer stockId,
                            Integer receiveAmount,
                            String partsName,
                            Integer centerId,
                            Integer categoryId,
                            String description) {
        String sql = "INSERT INTO parts_stock (stock_id, amount, name, center_id, category_id, description, delete_flag, create_date, update_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 0, NOW(), NOW())";
        jdbcTemplate.update(sql, stockId, receiveAmount, partsName, centerId, categoryId, description);
    }

    /**
     * 部品入出庫履歴登録（履歴テーブルは transaction_type 等を保持）
     */
    public void insertHistory(Integer stockId,
                              String transactionType,
                              String transactionDate,
                              String supplierName,
                              String purchaseOrderNo,
                              String operatorName,
                              Integer receiveAmount,
                              String remarks,
                              Integer amountBefore,
                              Integer amountAfter) {

        String sql = "INSERT INTO parts_stock_history (" +
                     "stock_id, transaction_type, transaction_date, amount_before, amount_change, amount_after, " +
                     "supplier_name, purchase_order_no, operator_name, remarks, delete_flag, create_date, update_date" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW())";

        jdbcTemplate.update(sql,
            stockId,
            transactionType,
            transactionDate,
            amountBefore,
            receiveAmount,
            amountAfter,
            supplierName,
            purchaseOrderNo,
            operatorName,
            remarks
        );
    }
}