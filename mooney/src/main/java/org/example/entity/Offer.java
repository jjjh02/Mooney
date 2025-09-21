package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    private double offerPrice;

    private int offerCnt;

    private String offerSide; // "BUY" / "SELL"

    private String offerStatus; // "PENDING" / "FILLED" / "CANCELED"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    public void isFilled() {
        this.offerStatus = "FILLED";
    }
}
