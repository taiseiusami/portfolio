ackage com.amoibeojt.api.service.partsstock;

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
 * 検索条件に基づいて部品在庫情報を取得する。
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
	 * トランザクションIDをログに含めてトレーシングを可能にする。
	 *
	 * @param searchCriteria 検索条件DTO
	 * @param transactionId トランザクションID（UUID v4形式）
	 * @return 条件に合致した在庫一覧
	 */
	public List<PartsStockResponseDTO> searchPartsStock(PartsStockSearchDTO searchCriteria, String transactionId) {
		LOGGER.info("部品在庫検索処理開始 Transaction-Id: {}", transactionId);

		List<PartsStockResponseDTO> resultList = repository.searchByCriteriaWithJoin(
			convertEmptyToNull(searchCriteria.getCenterId()),
			convertEmptyToNull(searchCriteria.getCategoryId()),
			convertEmptyToNull(searchCriteria.getStockId()),
			searchCriteria.getNamePattern(),
			searchCriteria.getAmountMin(),
			searchCriteria.getAmountMax()
		);

		LOGGER.info("部品在庫検索処理終了 件数: {} Transaction-Id: {}", resultList.size(), transactionId);
		return resultList;
	}

	/**
	 * 空のリストを null に変換するユーティリティメソッド。
	 * DB検索時に IN句の空リストを避けるために使用。
	 *
	 * @param list 対象リスト
	 * @return 空なら null、そうでなければ元のリスト
	 */
	private <T> List<T> convertEmptyToNull(List<T> list) {
		return (list == null || list.isEmpty()) ? null : list;
	}
}