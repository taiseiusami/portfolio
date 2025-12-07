package com.amoibeojt.api.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean existsByStockId(Integer stockId) {
        String sql = "SELECT COUNT(*) FROM parts_stock WHERE stock_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, stockId);
        return count != null && count > 0;
    }

    public Integer getCurrentAmount(Integer stockId) {
        String sql = "SELECT amount FROM parts_stock WHERE stock_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, stockId);
    }

    public Map<Integer, Integer> getAmountsByStockIds(List<Integer> stockIds) {
        if (stockIds == null || stockIds.isEmpty()) {
            return new HashMap<>();
        }
        String sql = "SELECT stock_id, amount FROM parts_stock WHERE stock_id IN (" +
                     stockIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("") +
                     ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, stockIds.toArray());
        Map<Integer, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer stockId = ((Number) row.get("stock_id")).intValue();
            Integer amount = ((Number) row.get("amount")).intValue();
            result.put(stockId, amount);
        }
        return result;
    }

    public void updateStock(Integer stockId,
                            Integer amountReceive,
                            String partsName,
                            Integer centerId,
                            Integer categoryId,
                            String description) {
        String sql = "UPDATE parts_stock SET amount = amount + ?, name = ?, center_id = ?, category_id = ?, description = ?, update_date = NOW() "
                   + "WHERE stock_id = ?";
        jdbcTemplate.update(sql, amountReceive, partsName, centerId, categoryId, description, stockId);
    }

    public void insertStock(Integer stockId,
                            Integer amountReceive,
                            String partsName,
                            Integer centerId,
                            Integer categoryId,
                            String description) {
        String sql = "INSERT INTO parts_stock (stock_id, amount, name, center_id, category_id, description, delete_flag, create_date, update_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 0, NOW(), NOW())";
        jdbcTemplate.update(sql, stockId, amountReceive, partsName, centerId, categoryId, description);
    }

    public void insertHistory(Integer stockId,
                              String transactionType,
                              String transactionDate,
                              String supplierName,
                              String purchaseOrderNo,
                              String operatorName,
                              Integer amountReceive,
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
            amountReceive,
            amountAfter,
            supplierName,
            purchaseOrderNo,
            operatorName,
            remarks
        );
    }
}