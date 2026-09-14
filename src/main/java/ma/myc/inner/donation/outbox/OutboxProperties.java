package ma.myc.inner.donation.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
        Duration pollInterval,
        int batchSize,
        int maxAttempts,
        Duration lockTtl,
        Duration initialBackoff,
        Duration maxBackoff
) {}