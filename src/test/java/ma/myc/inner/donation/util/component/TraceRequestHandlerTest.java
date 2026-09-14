package ma.myc.inner.donation.util.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class TraceRequestHandlerTest {
	@Mock
	private Tracer tracer;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private TraceRequestHandler traceRequestHandler;

	@Test
	void getCorrelationId_WhenTraceContextIsAvailable_ReturnsCorrelationId() {
		// Arrange
		CurrentTraceContext currentTraceContext = mock(CurrentTraceContext.class);
		TraceContext traceContext = mock(TraceContext.class);
		when(tracer.currentTraceContext()).thenReturn(currentTraceContext);
		when(currentTraceContext.context()).thenReturn(traceContext);
		when(traceContext.traceId()).thenReturn("testTraceId");
		// Act
		String correlationId = traceRequestHandler.getCorrelationId();
		// Assert
		assertEquals("testTraceId", correlationId);
	}

	@Test
    void getCorrelationId_WhenTraceContextIsNotAvailable_ReturnsNull() {
        // Arrange
        when(tracer.currentTraceContext()).thenReturn(null);
        // Act
        String correlationId = traceRequestHandler.getCorrelationId();
        // Assert
        assertEquals(null, correlationId);
    }

}
