package ma.myc.inner.donation.domain.core.error;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError implements Serializable {
	private static final long serialVersionUID = -8110282627123433453L;

	private String type; // * A URI reference, ex: /problem/client-not-found
	private String title; // * A short, human-readable summary of the problem type
	private String detail; // A human-readable explanation
	private String instance;// A URI reference that identifies the specific occurrence of the problem.
	private Integer status; // The HTTP status code
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String traceId; // Request Trace ID

	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<ApiErrorItem> errors = new ArrayList<>();

	public ApiError addError(String msg, String target) {
		this.errors.add(ApiErrorItem.builder()
				.message(msg)
				.target(target)
				.build());
		return this;
	}

	public ApiError addError(String msg, String target, Serializable[] params) {
		this.errors.add(ApiErrorItem.builder()
				.message(msg)
				.target(target)
				.params(Arrays.stream(params)
						.map(e -> ObjectUtils.defaultIfNull(e, "N/A"))
						.map(Objects::toString)
						.toArray(String[]::new))
				.build());
		return this;
	}
}
