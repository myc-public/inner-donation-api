package ma.myc.inner.donation.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import ma.myc.inner.donation.util.constants.ErrorConstants;

@Builder
public record VilleAddRequest(
		@NotBlank(message = ErrorConstants.ERR_CODE_VILLE_NOTBLANK) String code,
		String label,
		Boolean active) {
}
