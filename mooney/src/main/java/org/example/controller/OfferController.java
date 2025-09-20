package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.OfferDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offer")
@RequiredArgsConstructor
public class OfferController {

    private final KafkaTemplate<String, OfferDto> kafkaTemplate;

    @PostMapping()
    public void offerStock(@ModelAttribute OfferDto dto) {
        // @ModelAttribute → Thymeleaf 폼 데이터를 DTO로 자동 매핑
        // DTO의 필드와 폼 input name이 일치하면 자동 매핑
        kafkaTemplate.send("order-request", dto);
        System.out.println("📤 메세지 발행 : " + dto.getStockCode() + " " +
                dto.getOfferPrice() + " " +
                dto.getOfferCnt() + " " +
                dto.getOfferSide());
    }
}
