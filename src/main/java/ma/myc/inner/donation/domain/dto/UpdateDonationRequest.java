package ma.myc.inner.donation.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import ma.myc.inner.donation.domain.bo.DonationCategory;

import java.math.BigDecimal;

public record UpdateDonationRequest(
        DonationCategory category,
        Boolean type,
        @DecimalMin(value = "0.01") BigDecimal amount
) {}