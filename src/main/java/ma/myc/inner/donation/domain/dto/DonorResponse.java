package ma.myc.inner.donation.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DonorResponse(
        UUID donorId,
        String lastName,
        String firstName,
        String email,
        LocalDate dateOfBirth,
        String country
) {}