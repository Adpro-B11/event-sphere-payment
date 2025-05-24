package id.ac.ui.cs.advprog.eventspherepayment.client;

import id.ac.ui.cs.advprog.eventspherepayment.dto.BalanceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthServiceClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public AuthServiceClient(RestTemplate rest,
                             @Value("${auth.service.url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    public boolean addBalance(String userId, double amount) {
        BalanceRequest req = new BalanceRequest(userId, amount);
        HttpEntity<BalanceRequest> body = new HttpEntity<>(req, jsonHeaders());
        ResponseEntity<Void> resp = rest.exchange(
                baseUrl + "/users/" + userId + "/balance/add",
                HttpMethod.POST,
                body,
                Void.class
        );
        return resp.getStatusCode() == HttpStatus.OK;
    }

    public boolean deductBalance(String userId, double amount) {
        BalanceRequest req = new BalanceRequest(userId, amount);
        HttpEntity<BalanceRequest> body = new HttpEntity<>(req, jsonHeaders());
        ResponseEntity<Void> resp = rest.exchange(
                baseUrl + "/users/" + userId + "/balance/deduct",
                HttpMethod.POST,
                body,
                Void.class
        );
        return resp.getStatusCode() == HttpStatus.OK;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
