package ma.myc.inner.donation.domain.dto;

import lombok.Builder;

@Builder
public record VilleResponse(
		String code,
		String label,
		Boolean active) {
}
