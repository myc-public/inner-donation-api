package ma.myc.inner.donation.util.component;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

@Component
public class TraceRequestHandler {

	private final Tracer tracer;

	/*
	 * Constructor-based dependency injection is used to inject the Tracer bean
	 * required for trace operations.
	 *
	 * @param tracer: The Tracer instance provided by Micrometer for tracing
	 * operations.
	 */
	public TraceRequestHandler(Tracer tracer) {
		this.tracer = tracer;
	}

	/*
	 * getCorrelationId method retrieves the correlation ID from the current trace
	 * context, if available.
	 *
	 * @return String: The correlation ID retrieved from the trace context, or null
	 * if not available.
	 */
	public String getCorrelationId() {
		return Optional.ofNullable(tracer)
				.map(Tracer::currentTraceContext)
				.map(CurrentTraceContext::context)
				.map(TraceContext::traceId)
				.orElse(null);
	}
}
