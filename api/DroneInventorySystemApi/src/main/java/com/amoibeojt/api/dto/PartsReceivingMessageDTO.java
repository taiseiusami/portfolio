package com.amoibeojt.api.dto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;

/**
 * 部品入荷APIのエラーレスポンスDTO
 * - 400/500: errorCode, status, message, timestamp
 */
@Getter
public class PartsReceivingMessageDTO {

    private String errorCode; // 例: "400", "500"
    private String status;    // 例: "BAD_REQUEST", "INTERNAL_ERROR"
    private String message;   // 詳細なエラーメッセージ
    private String timestamp; // ISO形式のタイムスタンプ

    // コンストラクタ (errorCode, status, message の順)
    public PartsReceivingMessageDTO(String errorCode, String status, String message) {
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}