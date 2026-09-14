package ma.myc.inner.donation.events;


import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String aggregateType,
        String aggregateId,
        String producer,
        T payload
) {}