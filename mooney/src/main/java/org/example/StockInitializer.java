package org.example;

import lombok.RequiredArgsConstructor;
import org.example.entity.Stock;
import org.example.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@RequiredArgsConstructor
public class StockInitializer implements CommandLineRunner {

    private final StockRepository stockRepository;

    @Override
    public void run(String... args) throws Exception {
        if (stockRepository.count() == 0) {  // 테이블 비어 있으면
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/stocks_data.csv"), "UTF-8"))) {

                String line;
                br.readLine(); // 헤더 건너뛰기
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    Stock stock = Stock.builder()
                            .stockCode(tokens[0].trim())
                            .stockName(tokens[1].trim())
                            .build();
                    stockRepository.save(stock);
                }
                System.out.println("✅ stocks_simple.csv 데이터 DB 적재 완료");
            }
        } else {
            System.out.println("ℹ️ stocks 테이블에 이미 데이터가 있습니다.");
        }
    }
}
