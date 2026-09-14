package ma.myc.inner.donation;

import ma.myc.inner.donation.outbox.OutboxProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
public class MycInnerDonationApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MycInnerDonationApiApplication.class, args);
	}

}
