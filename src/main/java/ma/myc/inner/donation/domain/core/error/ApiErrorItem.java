package ma.myc.inner.donation.domain.core.error;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorItem implements Serializable {
	private static final long serialVersionUID = -3571893142664028282L;

	private String target;
	private String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String[] params;
}
