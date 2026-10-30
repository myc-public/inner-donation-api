package ma.myc.inner.donation.config;


import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@ConfigurationPropertiesScan
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}