package com.amoibeojt.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amoibeojt.api.dto.ApiResponse;
import com.amoibeojt.api.dto.PagedResponse;
import com.amoibeojt.api.dto.partsstock.PartsStockResponseDTO;
import com.amoibeojt.api.dto.partsstock.PartsStockSearchDTO;
import com.amoibeojt.api.exception.InvalidInputException;
import com.amoibeojt.api.service.partsstock.PartsStockService;
import com.amoibeojt.api.validator.PartsStockSearchValidator;

import lombok.RequiredArgsConstructor;

/**
 * 部品在庫照会 Controller
 * リクエストトレーシングのため Transaction-Id ヘッダーを検証・返却する。
 * 
 * @author your name
 * @return ページング結果
 */
@RestController
@RequestMapping("/api/parts/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartsStockController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartsStockController.class);

	private static final String HEADER_TRANSACTION_ID = "Transaction-Id";

	private final PartsStockService partsStockService;

	private final PartsStockSearchValidator validator;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	/**
	 * 部品在庫情報の検索
	 * Transaction-Id ヘッダーの検証とレスポンスヘッダーへの返却を行う。
	 * 
	 * @param request HTTPリクエスト
	 * @param response HTTPレスポンス
	 * @param centerId 拠点ID
	 * @param categoryId カテゴリID
	 * @param stockId 在庫ID
	 * @param namePattern 名称パターン
	 * @param amountMin 最小在庫数
	 * @param amountMax 最大在庫数
	 * @return 検索結果レスポンス
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<PartsStockResponseDTO>>> search(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestParam(value = "center_id", required = false) List<Integer> centerId,
		@RequestParam(value = "category_id", required = false) List<Integer> categoryId,
		@RequestParam(value = "stock_id", required = false) List<Integer> stockId,
		@RequestParam(value = "name_pattern", required = false) String namePattern,
		@RequestParam(value = "amount_min", required = false) Integer amountMin,
		@RequestParam(value = "amount_max", required = false) Integer amountMax
	) {

		// Transaction-Id ヘッダーの取得と検証
		String transactionId = request.getHeader(HEADER_TRANSACTION_ID);
		if (transactionId == null || transactionId.isBlank()) {
			LOGGER.warn("Transaction-Id ヘッダーが存在しない、または空です");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("error", "Transaction-Id ヘッダーが必須です", null));
		}

		if (!isValidUUIDv4(transactionId)) {
			LOGGER.warn("Transaction-Id ヘッダーが UUID v4 形式ではありません: {}", transactionId);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("error", "Transaction-Id は UUID v4 形式で指定してください", null));
		}

		// レスポンスヘッダーに Transaction-Id を設定
		response.setHeader(HEADER_TRANSACTION_ID, transactionId);

		// 検索条件DTOの作成とバリデーション
		PartsStockSearchDTO criteria = new PartsStockSearchDTO(centerId, categoryId, stockId, namePattern, amountMin, amountMax);
		Errors errors = new BeanPropertyBindingResult(criteria, "criteria");
		validator.validate(criteria, errors);
		if (errors.hasErrors()) {
			String code = errors.getAllErrors().get(0).getCode();
			throw new InvalidInputException(code);
		}

		// サービス呼び出しとレスポンス構築
		var list = partsStockService.search(criteria, transactionId);
		var page = new PagedResponse<>(list, list.size());

		if (page.getTotal_count() == 0) {
			var dummy = PartsStockResponseDTO.builder()
				.name("一致するデータがありません。")
				.build();
			page.setItems(List.of(dummy));
		}

		LOGGER.info("部品在庫情報の取得成功 Transaction-Id: {}", transactionId);

		return ResponseEntity.ok(new ApiResponse<>(
			"success",
			"部品在庫情報を正常に取得しました",
			page
		));
	}

	/**
	 * UUID v4 形式かどうかを検証する。
	 * 
	 * @param uuidStr 検証対象の文字列
	 * @return UUID v4 形式なら true
	 */
	private boolean isValidUUIDv4(String uuidStr) {
		try {
			UUID uuid = UUID.fromString(uuidStr);
			return uuid.version() == 4;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}