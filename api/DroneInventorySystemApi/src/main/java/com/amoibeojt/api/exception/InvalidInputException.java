package com.amoibeojt.api.exception;

import java.util.ResourceBundle;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 入力値不正例外
 * - メッセージキーを指定して messages.properties から解決可能
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends RuntimeException {

    private static final ResourceBundle messages = ResourceBundle.getBundle("messages");

    private final String messageKey;
    private final boolean treatAsKey;

    /**
     * 直接メッセージを渡す場合（従来の利用方法）
     */
    public InvalidInputException(String message) {
        super(message);
        this.messageKey = message;
        this.treatAsKey = false;
    }

    /**
     * メッセージキーを渡す場合（Validatorから呼び出す用）
     * treatAsKey = true の場合はキーとして扱い、messages.propertiesから解決する
     */
    public InvalidInputException(String messageKey, boolean treatAsKey) {
        super(treatAsKey ? messages.getString(messageKey) : messageKey);
        this.messageKey = messageKey;
        this.treatAsKey = treatAsKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public boolean isTreatAsKey() {
        return treatAsKey;
    }
}