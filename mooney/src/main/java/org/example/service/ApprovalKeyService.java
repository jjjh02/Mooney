package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalKeyService {
    // https://apiportal.koreainvestment.com/apiservice-apiservice?/oauth2/Approval
    // 웹소켓 접속 키를 발급을 위한 서비스 클래스

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kis.appkey}")
    private String appKey;

    @Value("${kis.secret}")
    private String secretKey;

    @Value("${kis.approval-url}")
    private String approvalUrl;

    private String approvalKey;
    private LocalDateTime expireAt;

    // approvalKey 반환
    public String getApprovalKey() {
        if (approvalKey == null || expireAt == null || LocalDateTime.now().isAfter(expireAt.minusMinutes(10))) {
            requestApprovalKey();
        }
        return approvalKey;
    }

    // 스케줄러로 1시간마다 자동 실행 -> 갱신
    @Scheduled(fixedRate = 1000 * 60 * 60) // 1시간마다 체크
    public void refreshKeyIfNeeded() {
        getApprovalKey();
    }

    // HTTP POST 요청으로 appkey와 secretkey를 API 서버에 전송 -> 응답: approval_key
    private void requestApprovalKey() {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var body = """
                {
                  "grant_type": "client_credentials",
                  "appkey": "%s",
                  "secretkey": "%s"
                }
                """.formatted(appKey, secretKey);

            var entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(approvalUrl, entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            this.approvalKey = json.get("approval_key").asText();
            this.expireAt = LocalDateTime.now().plusHours(24); // 보통 24시간

            System.out.println("✅ approval_key 발급 완료: " + approvalKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
