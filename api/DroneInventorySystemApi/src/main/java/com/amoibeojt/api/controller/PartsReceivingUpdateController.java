package com.amoibeojt.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amoibeojt.api.dto.ApiResponse;
import com.amoibeojt.api.dto.PartsReceivingMessageDTO;
import com.amoibeojt.api.dto.PartsReceivingUpdateDTO;
import com.amoibeojt.api.exception.InvalidInputException;
import com.amoibeojt.api.service.PartsReceivingUpdateService;
import com.amoibeojt.api.validator.PartsReceivingUpdateValidator;

import lombok.RequiredArgsConstructor;

/**
 * 部品入荷登録APIコントローラ
 * エンドポイント: POST /api/parts/stock/receive
 */
@RestController
@RequestMapping("/api/parts/stock/receive")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartsReceivingUpdateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartsReceivingUpdateController.class);

    private final PartsReceivingUpdateService partsReceivingUpdateService;
    private final PartsReceivingUpdateValidator validator;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody PartsReceivingUpdateDTO dto) {
        try {
            // バリデーション（InvalidInputExceptionを投げる設計）
            validator.validate(dto, null);

            // Service呼び出し
            partsReceivingUpdateService.register(dto);

            LOGGER.info("部品入荷処理成功");
            return ResponseEntity.ok(new ApiResponse<>("success", "部品入荷情報を正常に登録しました"));

        } catch (InvalidInputException e) {
            // 入力不正はここでキャッチして 400 を返す
            LOGGER.warn("入力値エラー: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PartsReceivingMessageDTO("400", "BAD_REQUEST", e.getMessage()));

        } catch (IllegalArgumentException e) {
            // パースエラーなど
            LOGGER.warn("不正なリクエスト: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PartsReceivingMessageDTO("400", "INVALID_REQUEST", "入力値が不正です"));

        } catch (Exception e) {
            // その他の予期せぬエラー
            LOGGER.error("部品入荷処理中に予期せぬエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new PartsReceivingMessageDTO("500", "INTERNAL_ERROR", "処理中にエラーが発生しました"));
        }
    }
}