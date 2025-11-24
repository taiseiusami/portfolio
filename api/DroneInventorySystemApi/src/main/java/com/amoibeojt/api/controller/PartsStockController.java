package com.amoibeojt.api.controller;

import java.util.ArrayList;
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

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<PartsStockResponseDTO>>> search(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestParam(value = "center_id", required = false) String centerIdRaw,
		@RequestParam(value = "category_id", required = false) String categoryIdRaw,
		@RequestParam(value = "stock_id", required = false) String stockIdRaw,
		@RequestParam(value = "name_pattern", required = false) String namePattern,
		@RequestParam(value = "amount_min", required = false) String amountMinRaw,
		@RequestParam(value = "amount_max", required = false) String amountMaxRaw
	) {
		String transactionId = request.getHeader(HEADER_TRANSACTION_ID);
		response.setHeader(HEADER_TRANSACTION_ID, transactionId);

		if (transactionId == null || transactionId.isBlank()) {
			LOGGER.warn("Transaction-Id ヘッダーが存在しない、または空です");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("error", "Transaction-Id ヘッダーが必須です", null));
		}
		if (!isValidUuidV4(transactionId)) {
			LOGGER.warn("Transaction-Id ヘッダーが UUID v4 形式ではありません: {}", transactionId);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("error", "Transaction-Id は UUID v4 形式で指定してください", null));
		}

		try {
			List<Integer> centerId = parseIntegerList(centerIdRaw, "center_id");
			List<Integer> categoryId = parseIntegerList(categoryIdRaw, "category_id");
			List<Integer> stockId = parseIntegerList(stockIdRaw, "stock_id");
			Integer amountMin = parseSingleInteger(amountMinRaw, "amount_min");
			Integer amountMax = parseSingleInteger(amountMaxRaw, "amount_max");

			PartsStockSearchDTO criteria = new PartsStockSearchDTO(centerId, categoryId, stockId, namePattern, amountMin, amountMax);
			Errors errors = new BeanPropertyBindingResult(criteria, "criteria");
			validator.validate(criteria, errors);
			if (errors.hasErrors()) {
				String code = errors.getAllErrors().get(0).getCode();
				throw new InvalidInputException(code); // ← ここはキーとして扱う
			}

			List<PartsStockResponseDTO> resultList = partsStockService.searchPartsStock(criteria, transactionId);
			PagedResponse<PartsStockResponseDTO> page = new PagedResponse<>(resultList, resultList.size());

			if (page.getTotal_count() == 0) {
				PartsStockResponseDTO dummy = PartsStockResponseDTO.builder()
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

		} catch (InvalidInputException e) {
			LOGGER.warn("入力値エラー: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("Bad Request", e.getMessage(), null));

		} catch (IllegalArgumentException e) {
			LOGGER.warn("不正なリクエスト: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>("INVALID_REQUEST", "不明なエラー", null));
		}
	}

	private List<Integer> parseIntegerList(String raw, String fieldName) {
		List<Integer> result = new ArrayList<>();
		if (raw == null || raw.isBlank()) return result;
		String[] tokens = raw.split(",");
		for (String token : tokens) {
			if (token == null || token.isBlank()) {
				LOGGER.warn("{} に空白の値が含まれています", fieldName);
				throw new InvalidInputException(fieldName + " のリストに空または非数値が含まれています");
			}
			try {
				result.add(Integer.parseInt(token));
			} catch (NumberFormatException e) {
				LOGGER.warn("{} に数値以外の値が含まれています: {}", fieldName, token);
				throw new IllegalArgumentException();
			}
		}
		return result;
	}

	private Integer parseSingleInteger(String raw, String fieldName) {
		if (raw == null || raw.isBlank()) return null;
		if (raw.isBlank()) {
			LOGGER.warn("{} に空白の値が含まれています", fieldName);
			throw new InvalidInputException(fieldName + " のリストに空または非数値が含まれています");
		}
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException e) {
			LOGGER.warn("{} に数値以外の値が含まれています: {}", fieldName, raw);
			throw new IllegalArgumentException();
		}
	}

	private boolean isValidUuidV4(String uuidStr) {
		try {
			UUID uuid = UUID.fromString(uuidStr);
			return uuid.version() == 4;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}