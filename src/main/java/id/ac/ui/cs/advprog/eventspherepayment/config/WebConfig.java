package id.ac.ui.cs.advprog.eventspherepayment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import java.util.Collections;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rest = new RestTemplate();

        ClientHttpRequestInterceptor jwtPropagation = (req, body, exec) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                String token = auth.getCredentials().toString();
                req.getHeaders().setBearerAuth(token);
            }
            return exec.execute(req, body);
        };

        rest.setInterceptors(Collections.singletonList(jwtPropagation));
        return rest;
    }
}
