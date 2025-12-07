package com.amoibeojt.api.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.amoibeojt.api.entity.PartsStock;
import com.amoibeojt.api.entity.PartsStockHistory;

import lombok.RequiredArgsConstructor;

/**
 * 部品在庫更新リポジトリ
 * 在庫と履歴をまとめて扱う
 */
@Repository
@RequiredArgsConstructor
public class PartsReceivingUpdateRepository {

    private final PartsStockJpa stockJpa;
    private final PartsStockHistoryJpa historyJpa;

    // 在庫操作
    public boolean existsByStockId(Integer stockId) {
        return stockJpa.existsByStockId(stockId);
    }

    public Integer getCurrentAmount(Integer stockId) {
        return stockJpa.getCurrentAmount(stockId);
    }

    public List<PartsStock> findByStockIds(List<Integer> stockIds) {
        return stockJpa.findByStockIds(stockIds);
    }

    public PartsStock saveStock(PartsStock stock) {
        return stockJpa.save(stock);
    }

    // 履歴操作
    public PartsStockHistory saveHistory(PartsStockHistory history) {
        return historyJpa.save(history);
    }
}