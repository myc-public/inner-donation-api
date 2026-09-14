package ma.myc.inner.donation.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record DonorSnapshot(
        @NotNull UUID donorId,
        LocalDate dateOfBirth,
        String country
) {
}
