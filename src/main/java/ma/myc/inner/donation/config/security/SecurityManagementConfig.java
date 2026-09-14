package ma.myc.inner.donation.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import ma.myc.inner.donation.config.properties.ManagementUsersProps;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class SecurityManagementConfig {
	private static final int ORDER = 1;

	private final SecurityAuthEntryPoint securityAuthEntryPoint;
	private final SecurityAccessDeniedHandler securityAccessDeniedHandler;
	private final ManagementUsersProps managementUsersProps;

	@Order(ORDER)
	@Bean
	@ConditionalOnProperty(prefix = "myc.security", name = "enabled", havingValue = "true", matchIfMissing = true)
	SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.securityMatcher("/management/**")
				.authorizeHttpRequests(authRequests -> authRequests
						.requestMatchers("/management/health").hasAnyRole("VIEWER", "ADMIN")
						.requestMatchers("/management/**").hasAnyRole("ADMIN"))
				.httpBasic(Customizer.withDefaults());
		http
				.formLogin(FormLoginConfigurer::disable)
				.logout(LogoutConfigurer::disable)
				.cors(withDefaults())
				.csrf(CsrfConfigurer::disable) // NOSONAR
				.exceptionHandling(handling -> handling
						.accessDeniedHandler(securityAccessDeniedHandler)
						.authenticationEntryPoint(securityAuthEntryPoint));
		return http.build();
	}

	@Bean
	InMemoryUserDetailsManager userDetailsService(AuthenticationManagerBuilder auth) {
		Collection<UserDetails> users = new ArrayList<>();
		for (ManagementUsersProps.UserMonitoring user : managementUsersProps.getUsers()) {
			users.add(User
					.withUsername(user.getUsername())
					.password(String.format("{noop}%s", user.getPassword()))
					.roles(user.getRole())
					.build());
		}
		return new InMemoryUserDetailsManager(users);
	}
}
