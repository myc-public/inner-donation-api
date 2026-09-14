package ma.myc.inner.donation.config;

import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.micrometer.tracing.Tracer;
import ma.myc.inner.donation.util.component.MsgSource;
import ma.myc.inner.donation.util.component.TraceRequestHandler;

@TestConfiguration
public class TestConfig {

	@Mock
	Tracer tracer;
	
	@Bean
	TraceRequestHandler traceRequestHandler() {
		return new TraceRequestHandler(tracer);
	}
	
	@Bean
	MsgSource msgSource() {
		return new MsgSource();
	}
}
