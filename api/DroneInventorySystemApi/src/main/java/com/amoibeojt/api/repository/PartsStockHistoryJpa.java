package com.amoibeojt.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.amoibeojt.api.entity.PartsStockHistory;

@Repository
public interface PartsStockHistoryJpa extends JpaRepository<PartsStockHistory, Long> {
}