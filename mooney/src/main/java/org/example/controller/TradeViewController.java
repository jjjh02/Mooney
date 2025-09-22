package org.example.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TradeViewController {

    @GetMapping("/trade")
    public String tradeView() {
        // templates/trade.html 렌더링
        return "trade";
    }
}
