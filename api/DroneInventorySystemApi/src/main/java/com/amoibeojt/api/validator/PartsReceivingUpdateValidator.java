package com.amoibeojt.api.validator;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import com.amoibeojt.api.dto.PartsReceivingUpdateDTO;
import com.amoibeojt.api.exception.InvalidInputException;

/**
 * 部品入荷 バリデーション
 */
@Component
public class PartsReceivingUpdateValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PartsReceivingUpdateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, org.springframework.validation.Errors errors) {
        PartsReceivingUpdateDTO dto = (PartsReceivingUpdateDTO) target;

        // 取引種別
        if (dto.getTransactionType() == null || !"RECEIVE".equals(dto.getTransactionType())) {
            throw new InvalidInputException("transaction_typeは必須項目です。");
        }

        // 入荷日時
        if (dto.getTransactionDate() == null || dto.getTransactionDate().isBlank()) {
            throw new InvalidInputException("transaction_dateは必須項目です。");
        }
        try {
            OffsetDateTime.parse(dto.getTransactionDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("transaction_dateの形式が不正です。");
        }

        // 仕入先名
        if (dto.getSupplierName() == null || dto.getSupplierName().isBlank()) {
            throw new InvalidInputException("supplier_nameは必須項目です。");
        }

        // 発注書番号
        if (dto.getPurchaseOrderNo() == null || dto.getPurchaseOrderNo().isBlank()) {
            throw new InvalidInputException("purchase_order_noは必須項目です。");
        }

        // 作業者名
        if (dto.getOperatorName() == null || dto.getOperatorName().isBlank()) {
            throw new InvalidInputException("operator_nameは必須項目です。");
        }

        // items
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new InvalidInputException("itemsは必須項目です。");
        }

        // items配列の中身チェック
        for (int i = 0; i < dto.getItems().size(); i++) {
            var item = dto.getItems().get(i);

            if (item.getStockId() == null || item.getStockId().isBlank()) {
                throw new InvalidInputException("stock_idは必須項目です。");
            }
            try {
                int stockId = Integer.parseInt(item.getStockId());
                if (stockId <= 0) throw new InvalidInputException("stock_idは1以上でなければなりません。");
            } catch (NumberFormatException e) {
                throw new InvalidInputException("stock_idは数値でなければなりません。");
            }

            if (item.getCenterId() == null || item.getCenterId().isBlank()) {
                throw new InvalidInputException("center_idは必須項目です。");
            }
            try {
                int centerId = Integer.parseInt(item.getCenterId());
                if (centerId <= 0) throw new InvalidInputException("center_idは1以上でなければなりません。");
            } catch (NumberFormatException e) {
                throw new InvalidInputException("center_idは数値でなければなりません。");
            }

            if (item.getCategoryId() == null || item.getCategoryId().isBlank()) {
                throw new InvalidInputException("category_idは必須項目です。");
            }
            try {
                int categoryId = Integer.parseInt(item.getCategoryId());
                if (categoryId <= 0) throw new InvalidInputException("category_idは1以上でなければなりません。");
            } catch (NumberFormatException e) {
                throw new InvalidInputException("category_idは数値でなければなりません。");
            }

            if (item.getPartsName() == null || item.getPartsName().isBlank()) {
                throw new InvalidInputException("parts_nameは必須項目です。");
            }

            if (item.getReceiveAmount() == null || item.getReceiveAmount().isBlank()) {
                throw new InvalidInputException("receive_amountは必須項目です。");
            }
            try {
                int receiveAmount = Integer.parseInt(item.getReceiveAmount());
                if (receiveAmount <= 0) throw new InvalidInputException("receive_amountは1以上でなければなりません。");
            } catch (NumberFormatException e) {
                throw new InvalidInputException("receive_amountは数値でなければなりません。");
            }
        }
    }
}