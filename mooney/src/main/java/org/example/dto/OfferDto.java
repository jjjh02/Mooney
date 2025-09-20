package org.example.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfferDto {
    // 종목코드
    private String stockCode;
    // 호가 (주문가)
    private int offerPrice;
    // 주문량
    private int offerCnt;
    // 매도 or 매수 여부
    private String offerSide;
}
