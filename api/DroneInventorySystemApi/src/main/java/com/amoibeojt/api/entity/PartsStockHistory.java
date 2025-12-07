package com.amoibeojt.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部品在庫履歴Entity
 */
@Entity
@Table(name = "parts_stock_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartsStockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "stock_id", nullable = false)
    private Integer stockId;

    @Column(name = "transaction_type", length = 50, nullable = false)
    private String transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "amount_before", nullable = false)
    private Integer amountBefore;

    @Column(name = "amount_change", nullable = false)
    private Integer amountChange;

    @Column(name = "amount_after", nullable = false)
    private Integer amountAfter;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "purchase_order_no", length = 100)
    private String purchaseOrderNo;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "delete_flag", nullable = false)
    private boolean deleteFlag;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;
}