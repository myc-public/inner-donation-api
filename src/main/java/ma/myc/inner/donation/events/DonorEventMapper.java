package ma.myc.inner.donation.events;


import ma.myc.inner.donation.domain.bo.DonorBO;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class DonorEventMapper {

    private final Clock clock;

    public DonorEventMapper(Clock clock) {
        this.clock = clock;
    }

    public EventEnvelope<DonorCreatedEventPayload> toDonorCreatedEnvelope(DonorBO donor, String producer) {
        Instant now = Instant.now(clock);

        DonorCreatedEventPayload payload = new DonorCreatedEventPayload(
                donor.getLastName(),
                donor.getFirstName(),
                donor.getEmail(),
                donor.getDateOfBirth(),
                donor.getCountry(),
                donor.getCreatedAt()
        );

        return new EventEnvelope<>(
                UUID.randomUUID(),
                "DonorCreated",
                "1",
                now,
                "Donor",
                donor.getId().toString(),
                producer,
                null,
                payload
        );
    }
}
