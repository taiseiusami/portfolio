package com.amoibeojt.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部品入荷登録用 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartsReceivingUpdateDTO {

    // 取引種別（RECEIVE固定）
    private String transactionType;
    // 入荷日時（ISO 8601 拡張形式）
    private String transactionDate;
    // 仕入先名
    private String supplierName;
    // 発注書番号
    private String purchaseOrderNo;
    // 作業者名
    private String operatorName;
    // 入荷部品リスト
    private List<PartsItemDTO> items;

    /**
     * 部品情報 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartsItemDTO {

        // 在庫ID（JSONでは数値だが、DTOではStringで受け取る）
        private String stockId;
        // センターID
        private String centerId;
        // カテゴリID
        private String categoryId;
        // 部品名
        private String partsName;
        // 入荷数量
        private String receiveAmount;
        // 部品説明（任意）
        private String description;
    }
}