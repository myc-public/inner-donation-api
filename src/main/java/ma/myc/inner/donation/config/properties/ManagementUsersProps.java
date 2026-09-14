package ma.myc.inner.donation.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Configuration
@ConfigurationProperties(prefix = "management.security")
public class ManagementUsersProps {
    private List<UserMonitoring> users = new ArrayList<>();
    
    @Data
	@ToString
	@EqualsAndHashCode
    public static class UserMonitoring {
    	private String username;
    	private String password;
    	private String role;
    }
}
