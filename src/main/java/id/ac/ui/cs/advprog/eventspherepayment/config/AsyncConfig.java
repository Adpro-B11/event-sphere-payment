package id.ac.ui.cs.advprog.eventspherepayment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor; // Import this

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor delegateExecutor = new ThreadPoolTaskExecutor();
        delegateExecutor.setCorePoolSize(5);
        delegateExecutor.setMaxPoolSize(10);
        delegateExecutor.setQueueCapacity(25);
        delegateExecutor.setThreadNamePrefix("EvtAsync-");
        delegateExecutor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(delegateExecutor);
    }
}