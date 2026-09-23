package ma.myc.inner.donation.outbox;


import ma.myc.inner.donation.events.EventEnvelope;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OutboxFactory {

    //TODO to refactor voir s'il y a des outils meilleurs dans kafka
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public OutboxFactory(JsonMapper jsonMapper, Clock clock) {
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }
    public OutboxEventBO newEvent(String topic, String key, EventEnvelope<?> envelope) {
        Instant now = Instant.now(clock);

        String payloadJson = writeJson(envelope);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("eventId", envelope.eventId().toString());
        headers.put("eventType", envelope.eventType());
        headers.put("eventVersion", envelope.eventVersion());
        headers.put("occurredAt", envelope.occurredAt().toString());
        headers.put("aggregateType", envelope.aggregateType());
        headers.put("aggregateId", envelope.aggregateId());

        // optionnels -> uniquement si non null / non blank
        putIfNotNull(headers, "producer", envelope.producer());

        String headersJson = writeJson(headers);

        return new OutboxEventBO(
                envelope.eventId(),
                envelope.aggregateType(),
                envelope.aggregateId(),
                envelope.eventType(),
                envelope.eventVersion(),
                topic,
                key,
                payloadJson,
                headersJson,
                now
        );
    }

    private static void putIfNotNull(Map<String, String> map, String k, String v) {
        if (v != null && !v.isBlank()) {
            map.put(k, v);
        }
    }
    private String writeJson(Object o) {
        try {
            return jsonMapper.writeValueAsString(o);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize event to JSON", e);
        }
    }
}