package ma.myc.inner.donation.domain.dto;


import ma.myc.inner.donation.domain.bo.DonationCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DonationResponse(
        UUID donationId,
        DonationCategory category,
        boolean type,
        BigDecimal amount,
        UUID donorId,
        Instant timestamp
) {}