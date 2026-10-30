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
        String traceId, // pose par OutboxFactory ; null hors trace (ADR 0037, DE35)
        T payload
) {

    public EventEnvelope<T> withTraceId(String value) {
        return new EventEnvelope<>(eventId, eventType, eventVersion, occurredAt, aggregateType, aggregateId,
                producer, value, payload);
    }
}
