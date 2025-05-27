package id.ac.ui.cs.advprog.eventspherepayment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Component
public class AuthServiceClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public AuthServiceClient(RestTemplate restTemplate,
                             @Value("${auth.service.base-url}") String baseUrl) {
        this.rest = restTemplate;
        this.rest.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
    }

    private String resolveJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String) {
            return (String) auth.getCredentials();
        }
        return null;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        String jwt = resolveJwt();
        if (jwt != null) {
            headers.setBearerAuth(jwt);
        }
        return headers;
    }

    public boolean addBalance(String userId, double amount) {
        String url = baseUrl + "/api/balance/{userId}/add";
        Map<String, Double> bodyMap = Map.of("amount", amount);
        HttpEntity<Map<String, Double>> body = new HttpEntity<>(bodyMap, jsonHeaders());
        try {
            ResponseEntity<Void> resp = rest.exchange(
                    url, HttpMethod.POST, body, Void.class, Map.of("userId", userId)
            );
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException ex) {
            return false;
        }
    }

    public boolean deductBalance(String userId, double amount) {
        String url = baseUrl + "/api/balance/{userId}/deduct";
        Map<String, Double> bodyMap = Map.of("amount", amount);
        HttpEntity<Map<String, Double>> body = new HttpEntity<>(bodyMap, jsonHeaders());
        try {
            ResponseEntity<Void> resp = rest.exchange(
                    url, HttpMethod.POST, body, Void.class, Map.of("userId", userId)
            );
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException ex) {
            return false;
        }
    }
}
