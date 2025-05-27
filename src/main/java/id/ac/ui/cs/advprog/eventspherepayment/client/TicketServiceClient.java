package id.ac.ui.cs.advprog.eventspherepayment.client;

import id.ac.ui.cs.advprog.eventspherepayment.dto.DeductTicketRequest;
import lombok.extern.slf4j.Slf4j; // Tambahkan ini
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Slf4j // Tambahkan ini
@Component
public class TicketServiceClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public TicketServiceClient(RestTemplate restTemplate,
                               @Value("${event.service.base-url}") String baseUrl) {
        this.rest = restTemplate;
        this.rest.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        log.info("TicketServiceClient initialized with baseUrl={}", this.baseUrl); // Logger saat init
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

    public boolean deductTickets(Map<String, String> ticketQuantities, String eventId) {
        DeductTicketRequest req = new DeductTicketRequest();
        req.setEventId(eventId);
        req.setTickets(ticketQuantities);
        HttpHeaders headers = jsonHeaders();
        log.debug("DeductTicketRequest body: eventId={}, tickets={}", req.getEventId(), req.getTickets());
        log.debug("DeductTicketRequest headers: {}", headers);
        HttpEntity<DeductTicketRequest> body = new HttpEntity<>(req, jsonHeaders());
        String url = baseUrl + "/api/tickets/deduct-batch";

        // Log detail request
        log.info("Calling ticket deduction. URL: {}", url);
        log.debug("DeductTicketRequest: eventId={}, tickets={}", eventId, ticketQuantities);

        try {
            ResponseEntity<Void> resp = rest.exchange(
                    url, HttpMethod.POST, body, Void.class
            );
            log.info("Ticket deduction response status: {}", resp.getStatusCode());
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException ex) {
            log.error("Ticket deduction failed! URL: {}, error: {}", url, ex.getMessage());
            return false;
        }
    }
}
