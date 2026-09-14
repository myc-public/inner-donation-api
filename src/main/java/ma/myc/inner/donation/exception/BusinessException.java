package ma.myc.inner.donation.exception;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ma.myc.inner.donation.domain.core.error.ApiError;

@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends Exception {
	private static final long serialVersionUID = 1L;

	private List<ExceptionMessage> messages;

	public BusinessException(List<ExceptionMessage> exceptionMessages) {
		this.messages = exceptionMessages;
	}

	public BusinessException(String target, String message) {
		this.messages = Collections.singletonList(ExceptionMessage.builder()
				.target(target)
				.message(message)
				.build());
	}

	public BusinessException(String target, String message, Serializable... params) {
		this.messages = Collections.singletonList(ExceptionMessage.builder()
				.target(target)
				.message(message)
				.params(params)
				.build());
	}

	public BusinessException(ApiError apiError) {
		this.messages = CollectionUtils.emptyIfNull(apiError.getErrors())
				.stream()
				.map(e -> ExceptionMessage.builder()
						.target(e.getTarget())
						.message(e.getMessage())
						.params(e.getParams())
						.build())
				.toList();
	}

	@Override
	public String getMessage() {
		return CollectionUtils.emptyIfNull(messages)
				.stream()
				.map(ExceptionMessage::getMessage)
				.collect(Collectors.joining(", "));
	}
}
