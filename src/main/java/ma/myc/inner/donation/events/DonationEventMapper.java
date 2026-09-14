package ma.myc.inner.donation.events;


import ma.myc.inner.donation.domain.bo.DonationBO;
import ma.myc.inner.donation.domain.bo.DonorBO;
import ma.myc.inner.donation.domain.dto.DonorSnapshot;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class DonationEventMapper {

    private final Clock clock;

    public DonationEventMapper(Clock clock) {
        this.clock = clock;
    }

    public EventEnvelope<DonationCreatedEventPayload> toDonationCreatedEnvelope(
            DonationBO donation,
            DonorSnapshot donor,
            String producer
    ) {
        Instant now = Instant.now(clock);
        UUID eventId = UUID.randomUUID();

        DonationCreatedEventPayload payload = new DonationCreatedEventPayload(
                        donation.getCategory().name(),
                        donation.isType(),
                        donation.getAmount(),
                        donation.getTimestamp(),
                donor.donorId(),
                donor.dateOfBirth(),
                donor.country()
                );

        return new EventEnvelope<>(
                eventId,
                "DonationCreated",
                "1",
                now,
                "Donation",
                donation.getId().toString(),
                producer,
                payload
        );
    }
}