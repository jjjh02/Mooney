package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.OfferDto;
import org.example.entity.Account;
import org.example.entity.Offer;
import org.example.entity.Stock;
import org.example.entity.User;
import org.example.repository.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferKafkaConsumer {

    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    @KafkaListener(topics = "order-request", groupId = "mooney-offer-group")
    public void saveOffer(OfferDto dto) {
        System.out.println("📥 메세지 구독 : " + dto.getStockCode() + " " +
                dto.getOfferPrice() + " " +
                dto.getOfferCnt() + " " +
                dto.getOfferSide());

        // Stock, Account 데이터 조회
        Stock stock = stockRepository.findByStockCode(dto.getStockCode());
        Optional<User> user = userRepository.findById(1L);
        Account account = accountRepository.findByUser(user);

        // 1. 주문 테이블에 저장
        Offer offer = dto.toEntity(dto, stock, account);
        offerRepository.save(offer);

        // 2. 체결 테이블에 저장 (PENDING 상태)
        tradeRepository.save(dto.addTradeEntity(offer));
    }
}
