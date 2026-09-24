package ma.myc.inner.donation.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.ArrayList;
import java.util.Collection;

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

	// Toujours active, independamment de myc.security.enabled (securite metier) : les endpoints de
	// management (loggers, metrics...) ne doivent jamais etre publics.
	@Order(ORDER)
	@Bean
	SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.securityMatcher("/management/**")
				.authorizeHttpRequests(authRequests -> authRequests
						// Sondes Kubernetes et statut global : publics (details reserves aux authentifies, cf. show-details)
						.requestMatchers("/management/health", "/management/health/**", "/management/info").permitAll()
						.requestMatchers("/management/prometheus", "/management/metrics/**").hasAnyRole("VIEWER", "ADMIN")
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
