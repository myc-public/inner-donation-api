package ma.myc.inner.donation.events;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import ma.myc.inner.donation.domain.bo.DonationBO;
import ma.myc.inner.donation.domain.bo.DonationCategory;
import ma.myc.inner.donation.domain.bo.DonorBO;
import ma.myc.inner.donation.domain.dto.DonorSnapshot;
import ma.myc.inner.donation.outbox.OutboxEventBO;
import ma.myc.inner.donation.outbox.OutboxFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contrat publie (ADR 0035, EDA-T01) : le JSON reellement ecrit en outbox est valide contre le schema versionne.
 */
class EventSchemasTest {

    private static final Instant NOW = Instant.parse("2026-10-30T10:00:00.123456Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final OutboxFactory outboxFactory =
            new OutboxFactory(JsonMapper.builder().build(), CLOCK, Tracer.NOOP, Propagator.NOOP);

    @Test
    @DisplayName("DonorCreated : conforme a donor-created-v1")
    void donorCreated_matchesSchema() throws IOException {
        var donor = new DonorBO(UUID.randomUUID(), "Doe", "John", "john.doe@example.com",
                LocalDate.of(1990, 1, 1), "MA", NOW);

        OutboxEventBO event = outboxFactory.newEvent(
                new DonorEventMapper(CLOCK).toDonorCreatedEnvelope(donor, "inner-donation-api"));

        assertThat(event.getAggregateType()).isEqualTo("Donor");
        assertThat(event.getAggregateId()).isEqualTo(donor.getId().toString());
        assertThat(schema("donor-created-v1").validate(event.getPayload(), InputFormat.JSON)).isEmpty();
    }

    @Test
    @DisplayName("DonorCreated : les champs optionnels du donateur peuvent etre absents")
    void donorCreated_optionalFieldsNull_matchesSchema() throws IOException {
        var donor = new DonorBO(UUID.randomUUID(), "Doe", "John", "john.doe@example.com", null, null, NOW);

        OutboxEventBO event = outboxFactory.newEvent(
                new DonorEventMapper(CLOCK).toDonorCreatedEnvelope(donor, "inner-donation-api"));

        assertThat(schema("donor-created-v1").validate(event.getPayload(), InputFormat.JSON)).isEmpty();
    }

    @Test
    @DisplayName("DonationCreated : conforme a donation-created-v1")
    void donationCreated_matchesSchema() throws IOException {
        var donorId = UUID.randomUUID();
        var donation = new DonationBO(UUID.randomUUID(), DonationCategory.FOOD, false, new BigDecimal("12.50"),
                donorId, NOW);

        OutboxEventBO event = outboxFactory.newEvent(new DonationEventMapper(CLOCK).toDonationCreatedEnvelope(
                donation, new DonorSnapshot(donorId, LocalDate.of(1990, 1, 1), "MA"), "inner-donation-api"));

        assertThat(schema("donation-created-v1").validate(event.getPayload(), InputFormat.JSON)).isEmpty();
    }

    @Test
    @DisplayName("Un champ hors contrat est refuse par le schema")
    void schema_rejectsUnknownField() throws IOException {
        String event = """
                {"eventId":"11111111-1111-4111-8111-111111111111","eventType":"DonorCreated","eventVersion":"1",
                 "occurredAt":"2026-10-30T10:00:00Z","aggregateType":"Donor","aggregateId":"x","producer":null,
                 "traceId":null,"horsContrat":true,"payload":{}}""";

        assertThat(schema("donor-created-v1").validate(event, InputFormat.JSON)).isNotEmpty();
    }

    private static Schema schema(String name) throws IOException {
        try (InputStream in = EventSchemasTest.class.getResourceAsStream("/events/" + name + ".schema.json")) {
            return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                    .getSchema(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
