package ma.myc.inner.donation.exception;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import ma.myc.inner.donation.domain.core.error.ApiErrorItem;

@Data
@Builder
public class ExceptionMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private String target;
	private String message;
	private Serializable[] params;

	public ExceptionMessage(String target, String message) {
		this.target = target;
		this.message = message;
	}

	public ExceptionMessage(String target, String message, Serializable... params) {
		this.target = target;
		this.message = message;
		this.params = params;
	}

	public ExceptionMessage(ApiErrorItem errorItem) {
		this.target = errorItem.getTarget();
		this.message = errorItem.getMessage();
		this.params = errorItem.getParams();
	}

}
