package id.ac.ui.cs.advprog.eventspherepayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class EventSpherePaymentApplication {

    public static void main(String[] args) {

        try {
            Dotenv dotenv = Dotenv.load();
            System.setProperty("spring.datasource.url", dotenv.get("DB_URL"));
            System.setProperty("spring.datasource.username", dotenv.get("DB_USERNAME"));
            System.setProperty("spring.datasource.password", dotenv.get("DB_PASSWORD"));
            System.setProperty("jwt.secret-key", dotenv.get("JWT_SECRET_KEY"));
            System.setProperty("jwt.expiration", dotenv.get("JWT_EXPIRATION"));
            System.setProperty("auth.service.base-url", dotenv.get("AUTH_SERVICE_BASE_URL"));
            System.setProperty("event.service.base-url", dotenv.get("EVENT_SERVICE_BASE_URL"));
            System.out.println("SUCCESS: .env file loaded successfully");
            System.out.println("Using DB URL: " + System.getProperty("spring.datasource.url"));
            System.out.println("DB Username: " + System.getProperty("spring.datasource.username"));

        } catch (Exception e) {
            System.out.println("ERROR: .env file not found");
        }

        SpringApplication.run(EventSpherePaymentApplication.class, args);
    }



}
