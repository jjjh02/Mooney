package org.example.repository;

import org.example.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByStock_StockCodeAndOfferStatus(String stockCode, String pending);

    @Query("SELECT DISTINCT o.stock.stockCode FROM Offer o WHERE o.offerStatus = :status")
    List<String> findDistinctStockCodesByOfferStatus(@Param("status") String status);
}
