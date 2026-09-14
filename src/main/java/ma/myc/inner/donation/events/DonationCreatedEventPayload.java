package ma.myc.inner.donation.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DonationCreatedEventPayload(
        // donation
        String category,
        boolean type,
        BigDecimal amount,
        Instant timestamp,
        // donor snapshot
        UUID donorID,
        LocalDate dateOfBirth,
        String country
) {

}