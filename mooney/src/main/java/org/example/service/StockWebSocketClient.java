package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@ClientEndpoint
@RequiredArgsConstructor
public class StockWebSocketClient {

    private final ApprovalKeyService approvalKeyService;
    private final OfferService offerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kis.websocket-url}")
    private String websocketUrl;

    private Session userSession;

    // 애플리케이션이 켜지면 곧바로 WebSocket 연결 시도
    @PostConstruct
    public void init() {
        connect();
    }

    // 이 클래스 자체가 @ClientEndpoint이므로 연결 -> @OnOpen, @OnMessage 등이 콜백됨
    private void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(websocketUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 대기 주문(PENDING) 상태인 종목 코드들을 offerService에서 조회
    // 각 종목마다 subscribeStock() 호출 → 해당 종목 체결가 실시간 구독 요청
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("✅ WebSocket Connected");
        this.userSession = session;

        List<String> pendingStockCodes = offerService.getPendingStockCodes();
        for (String stockCode : pendingStockCodes) {
            subscribeStock(stockCode);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);

            if (json.has("output")) {
                JsonNode output = json.get("output");
                String stockCode = output.get("stck_shrn_iscd").asText();
                double price = output.get("stck_prpr").asDouble();
                String time = output.get("trd_tm").asText();

                offerService.matchOrders(stockCode, price);

                System.out.printf("📊 [%s] 체결가: %d (%s)%n", stockCode, price, time);
            } else {
                System.out.println("📨 수신: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 종목별 구독 요청
    public void subscribeStock(String stockCode) {
        try {
            String approvalKey = approvalKeyService.getApprovalKey();

            String subscribeMsg = """
            {
              "header": {
                "approval_key": "%s",
                "custtype": "P",
                "tr_type": "1",
                "content-type": "utf-8"
              },
              "body": {
                "input": {
                  "tr_id": "H0STCNT0",
                  "tr_key": "%s"
                }
              }
            }
            """.formatted(approvalKey, stockCode);

            if (userSession != null && userSession.isOpen()) {
                userSession.getBasicRemote().sendText(subscribeMsg);
                System.out.println("📩 종목 구독 요청 보냄: " + stockCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("❌ WebSocket Closed: " + reason);
        // 필요시 재연결 로직 추가
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}