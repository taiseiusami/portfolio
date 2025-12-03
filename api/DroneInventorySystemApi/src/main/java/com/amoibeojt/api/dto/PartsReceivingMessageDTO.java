package com.amoibeojt.api.dto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;

/**
 * 部品入荷APIのエラーレスポンスDTO
 * - 400/500: errorCode, message, status, timestamp
 */
@Getter
public class PartsReceivingMessageDTO {

    private String errorCode; // 400/500用
    private String message;   // 共通
    private String status;    // 共通
    private String timestamp; // 400/500用

    // 400/500用コンストラクタ
    public PartsReceivingMessageDTO(String errorCode, String message, String status) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}