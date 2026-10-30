package ma.myc.inner.donation.events;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Etat complet du donateur (Event-Carried State Transfer, ADR 0035) : donnees personnelles, jamais dans les logs.
 */
public record DonorCreatedEventPayload(
        String lastName,
        String firstName,
        String email,
        LocalDate dateOfBirth,
        String country,
        Instant createdAt
) {

}
