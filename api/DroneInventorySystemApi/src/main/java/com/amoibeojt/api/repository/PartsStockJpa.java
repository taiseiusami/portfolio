package com.amoibeojt.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.amoibeojt.api.entity.PartsStock;

@Repository
public interface PartsStockJpa extends JpaRepository<PartsStock, Integer> {

    boolean existsByStockId(Integer stockId);

    @Query("SELECT p.amount FROM PartsStock p WHERE p.stockId = :stockId")
    Integer getCurrentAmount(@Param("stockId") Integer stockId);

    @Query("SELECT p FROM PartsStock p WHERE p.stockId IN :stockIds")
    List<PartsStock> findByStockIds(@Param("stockIds") List<Integer> stockIds);
}