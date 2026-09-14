package ma.myc.inner.donation.exception;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class TechnicalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TechnicalException(Throwable e) {
		super(e);
	}

	public TechnicalException(String message) {
		super(message);
	}

	public TechnicalException() {
		super();
	}

	public static Supplier<TechnicalException> newInstance(String message) {
		return () -> new TechnicalException(message);
	}

}
