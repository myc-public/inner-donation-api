package ma.myc.inner.donation.config.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import ma.myc.inner.donation.events.DonationCreatedEventPayload;
import ma.myc.inner.donation.events.EventEnvelope;
import ma.myc.inner.donation.outbox.OutboxEventSaved;
import tools.jackson.databind.json.JsonMapper;

class OutboxMetricsListenerTest {

	private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
	private final JsonMapper jsonMapper = JsonMapper.builder().build();
	private final OutboxMetricsListener listener = new OutboxMetricsListener(registry, jsonMapper);

	@Test
	@DisplayName("DonationCreated : compte l'evenement, le don (categorie, pays) et agrege le montant")
	void donationCreated_recordsDomainAndBusinessMetrics() {
		listener.onOutboxEventSaved(donationCreated("HEALTH", "MA", new BigDecimal("150.50")));
		listener.onOutboxEventSaved(donationCreated("HEALTH", "MA", new BigDecimal("49.50")));

		assertThat(registry.get("domain.events").tag("event_type", "DonationCreated").tag("aggregate_type", "Donation")
				.counter().count()).isEqualTo(2.0);
		assertThat(registry.get("donations.created").tag("category", "HEALTH").tag("country", "MA")
				.counter().count()).isEqualTo(2.0);
		DistributionSummary amount = registry.get("donations.amount").tag("category", "HEALTH").summary();
		assertThat(amount.count()).isEqualTo(2);
		assertThat(amount.totalAmount()).isEqualTo(200.0);
	}

	@Test
	@DisplayName("Aucune etiquette a forte cardinalite : ni identifiant de donateur, ni montant en etiquette")
	void donationCreated_usesOnlyLowCardinalityTags() {
		listener.onOutboxEventSaved(donationCreated("FOOD", "FR", BigDecimal.TEN));

		// isNotEmpty : le test echoue si aucune metrique n'a ete creee (sinon allSatisfy passerait a vide)
		assertThat(registry.getMeters()).isNotEmpty().allSatisfy(meter -> assertThat(meter.getId().getTags())
				.isNotEmpty()
				.allSatisfy(tag -> assertThat(tag.getKey()).isIn("event_type", "aggregate_type", "category", "country")));
	}

	@Test
	@DisplayName("Autre type d'evenement : seul le compteur generique domain.events est incremente")
	void otherEvent_onlyCountsDomainEvent() {
		listener.onOutboxEventSaved(new OutboxEventSaved("DonorUpdated", "Donor", "{}"));

		assertThat(registry.get("domain.events").tag("event_type", "DonorUpdated").counter().count()).isEqualTo(1.0);
		assertThat(registry.find("donations.created").counter()).isNull();
	}

	@Test
	@DisplayName("Payload illisible : la telemetrie ne leve jamais d'exception vers le metier")
	void unreadablePayload_neverThrows() {
		assertThatNoException().isThrownBy(
				() -> listener.onOutboxEventSaved(new OutboxEventSaved("DonationCreated", "Donation", "not-json")));
	}

	@Test
	@DisplayName("Categorie ou pays absent : etiquette 'unknown'")
	void missingFields_areTaggedUnknown() {
		listener.onOutboxEventSaved(donationCreated(null, null, BigDecimal.ONE));

		assertThat(registry.get("donations.created").tag("category", "unknown").tag("country", "unknown")
				.counter().count()).isEqualTo(1.0);
	}

	private OutboxEventSaved donationCreated(String category, String country, BigDecimal amount) {
		var payload = new DonationCreatedEventPayload(category, false, amount, Instant.parse("2026-09-26T10:00:00Z"),
				UUID.randomUUID(), LocalDate.of(1990, 1, 1), country);
		var envelope = new EventEnvelope<>(UUID.randomUUID(), "DonationCreated", "1",
				Instant.parse("2026-09-26T10:00:00Z"), "Donation", UUID.randomUUID().toString(), "inner-donation-api", payload);
		return new OutboxEventSaved("DonationCreated", "Donation", jsonMapper.writeValueAsString(envelope));
	}
}
