package id.ac.ui.cs.advprog.eventspherepayment.auth.service;

import id.ac.ui.cs.advprog.eventspherepayment.auth.model.Role;
import id.ac.ui.cs.advprog.eventspherepayment.auth.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Value("${auth.service.url:http://localhost:8080}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;

    public UserService() {
        this.restTemplate = new RestTemplate();
    }

    public User getUserByToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl + "/api/users/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {
                return mapToUser(response.getBody());
            }
        } catch (Exception e) {
            // Log error or handle appropriately
        }
        return null;
    }

    public User getUserById(String userId) {
        try {
            String url = authServiceUrl + "/api/users/" + userId;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );

            if (response.getBody() != null) {
                return mapToUser(response.getBody());
            }
        } catch (Exception e) {
        }
        return null;
    }

    private User mapToUser(Map<String, Object> userMap) {
        return User.builder()
                .id(UUID.fromString((String) userMap.get("id")))
                .username((String) userMap.get("username"))
                .email((String) userMap.get("email"))
                .phoneNumber((String) userMap.get("phoneNumber"))
                .role(Role.valueOf((String) userMap.get("role")))
                .balance(new BigDecimal(userMap.get("balance").toString()))
                .build();
    }
}