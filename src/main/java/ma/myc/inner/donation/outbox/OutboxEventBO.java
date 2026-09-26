package ma.myc.inner.donation.outbox;

import jakarta.persistence.*;
import org.springframework.data.domain.DomainEvents;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEventBO {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID id; // eventId

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id" , nullable = false, length = 80)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "event_version", nullable = false, length = 20)
    private String eventVersion;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "message_key", nullable = false, length = 200)
    private String messageKey;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON

    @Lob
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers; // JSON optionnel

    @Column(name= "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;


    protected OutboxEventBO() {
    }

    public OutboxEventBO(UUID id,
                         String aggregateType,
                         String aggregateId,
                         String eventType,
                         String eventVersion,
                         String topic,
                         String messageKey,
                         String payload,
                         String headers,
                         Instant occurredAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.headers = headers;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getPayload() {
        return payload;
    }

    public String getHeaders() {
        return headers;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * Publie par Spring Data a chaque {@code save} : consomme apres commit (metriques metier derivees des
     * evenements du domaine, cf. OutboxMetricsListener). Aucun couplage des services a la telemetrie.
     */
    @DomainEvents
    public Collection<Object> domainEvents() {
        return List.of(new OutboxEventSaved(eventType, aggregateType, payload));
    }


}