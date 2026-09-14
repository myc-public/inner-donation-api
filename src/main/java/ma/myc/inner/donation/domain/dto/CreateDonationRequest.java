package ma.myc.inner.donation.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ma.myc.inner.donation.domain.bo.DonationCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDonationRequest(
        @NotNull DonationCategory category,
        boolean type,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull @Valid DonorSnapshot donor
) {}