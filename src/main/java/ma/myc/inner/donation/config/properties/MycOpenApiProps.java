package ma.myc.inner.donation.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "myc.docs.security")
public class MycOpenApiProps {
	private String schema;
	private String tokenUri;
	private String authUri;

}
