package com.amoibeojt.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APIの共通レスポンス
 *
 * @author yure name
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // nullフィールドはJSONに含めない
public class ApiResponse<T> {

    // "success" or "error"
    private String status;
    // 処理結果メッセージ
    private String message;
    // 実際のペイロード（nullの場合はレスポンスに含まれない）
    private T data;

    
    // status と message だけを受け取るコンストラクタ
    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}