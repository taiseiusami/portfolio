package com.amoibeojt.api.service.partsstock;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amoibeojt.api.dto.partsstock.PartsStockResponseDTO;
import com.amoibeojt.api.dto.partsstock.PartsStockSearchDTO;
import com.amoibeojt.api.repository.PartsStockRepository;

import lombok.RequiredArgsConstructor;

/**
 * 部品在庫照会のサービスクラス（DTO にマッピング）
 * リクエストトレーシングのため Transaction-Id をログ出力に含める。
 * 
 * @author your name
 */
@Service
@RequiredArgsConstructor
public class PartsStockService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartsStockService.class);

	private final PartsStockRepository repository;

	/**
	 * 検索条件に合致する在庫情報を取得する。
	 * @param search 検索条件DTO
	 * @param transactionId トランザクションID（UUID v4形式）
	 * @return 条件に合致した在庫一覧
	 */
	public List<PartsStockResponseDTO> search(PartsStockSearchDTO search, String transactionId) {

		// トレーシングログ出力（INFOレベル）
		LOGGER.info("部品在庫検索開始 Transaction-Id: {}", transactionId);

		List<PartsStockResponseDTO> resultList = repository.searchByCriteriaWithJoin(
			emptyToNull(search.getCenterId()),
			emptyToNull(search.getCategoryId()),
			emptyToNull(search.getStockId()),
			search.getNamePattern(),
			search.getAmountMin(),
			search.getAmountMax()
		);

		LOGGER.info("部品在庫検索終了 Transaction-Id: {} 件数: {}", transactionId, resultList.size());

		return resultList;
	}

	/**
	 * 空リストを null に変換する。
	 * @param list 対象リスト
	 * @return null または元のリスト
	 */
	private <T> List<T> emptyToNull(List<T> list) {
		return (list == null || list.isEmpty()) ? null : list;
	}
}