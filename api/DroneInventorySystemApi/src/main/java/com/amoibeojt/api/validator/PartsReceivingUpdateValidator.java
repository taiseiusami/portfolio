package com.amoibeojt.api.validator;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import com.amoibeojt.api.dto.PartsReceivingUpdateDTO;
import com.amoibeojt.api.exception.InvalidInputException;

/**
 * 部品入荷更新 バリデーション
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
            throw new InvalidInputException("transactionTypeIsRequiredOrInvalid", true);
        }

        // 入荷日時
        if (dto.getTransactionDate() == null || dto.getTransactionDate().isBlank()) {
            throw new InvalidInputException("transactionDateIsRequired", true);
        }
        try {
            OffsetDateTime.parse(dto.getTransactionDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("transactionDateInvalidFormat", true);
        }

        // 仕入先名
        if (dto.getSupplierName() == null || dto.getSupplierName().isBlank()) {
            throw new InvalidInputException("supplierNameIsRequired", true);
        }

        // 発注書番号
        if (dto.getPurchaseOrderNo() == null || dto.getPurchaseOrderNo().isBlank()) {
            throw new InvalidInputException("purchaseOrderNoIsRequired", true);
        }

        // 作業者名
        if (dto.getOperatorName() == null || dto.getOperatorName().isBlank()) {
            throw new InvalidInputException("operatorNameIsRequired", true);
        }

        // items
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new InvalidInputException("itemsIsRequired", true);
        }

        // items配列の中身チェック
        for (var item : dto.getItems()) {

            validatePositiveInteger(
                item.getStockId(),
                "stockIdIsRequired",
                "stockIdMustBePositive",
                "stockIdMustBeNumeric",
                "stockIdTooLarge"
            );

            validatePositiveInteger(
                item.getCenterId(),
                "centerIdIsRequired",
                "centerIdMustBePositive",
                "centerIdMustBeNumeric",
                "centerIdTooLarge"
            );

            validatePositiveInteger(
                item.getCategoryId(),
                "categoryIdIsRequired",
                "categoryIdMustBePositive",
                "categoryIdMustBeNumeric",
                "categoryIdTooLarge"
            );

            // partsName 必須
            if (item.getPartsName() == null || item.getPartsName().isBlank()) {
                throw new InvalidInputException("partsNameIsRequired", true);
            }

            // partsName 文字数チェック（1〜50）
            if (item.getPartsName().length() > 50) {
                throw new InvalidInputException("invalid.input.name", true);
            }

            validatePositiveInteger(
                item.getAmountReceive(),
                "amountReceiveIsRequired",
                "amountReceiveMustBePositive",
                "amountReceiveMustBeNumeric",
                "amountReceiveTooLarge"
            );

            // amountReceive 上限チェック（1〜9999）
            try {
                int amount = Integer.parseInt(item.getAmountReceive());
                if (amount > 9999) {
                    throw new InvalidInputException("invalid.input.amount", true);
                }
            } catch (NumberFormatException e) {
                // validatePositiveInteger が拾うので何もしない
            }

            // description 文字数チェック（1〜500）
            if (item.getDescription() != null && !item.getDescription().isBlank()) {
                if (item.getDescription().length() > 500) {
                    throw new InvalidInputException("remarksInvalidLength", true);
                }
            }
        }
    }

    /**
     * 正の整数チェック + 最大値チェック
     */
    private void validatePositiveInteger(String value,
                                         String messageKeyRequired,
                                         String messageKeyPositive,
                                         String messageKeyNumeric,
                                         String messageKeyTooLarge) {

        // 必須チェック
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(messageKeyRequired, true);
        }

        try {
            long longValue = Long.parseLong(value);

            if (longValue > Integer.MAX_VALUE) {
                throw new InvalidInputException(messageKeyTooLarge, true);
            }

            int parsed = (int) longValue;

            if (parsed <= 0) {
                throw new InvalidInputException(messageKeyPositive, true);
            }

        } catch (NumberFormatException e) {
            throw new InvalidInputException(messageKeyNumeric, true);
        }
    }
}