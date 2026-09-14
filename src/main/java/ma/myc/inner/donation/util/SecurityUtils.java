package ma.myc.inner.donation.util;

import java.util.Map;

public class SecurityUtils {

	private SecurityUtils() throws InstantiationException {
		throw new InstantiationException("Instances of this type are forbidden");
	}

	/**
	 * Computes the value for the WWW-Authenticate header based on the provided
	 * parameters. This method is designed following the OAuth 2.0 Authorization
	 * Framework RFC (RFC 6750).
	 *
	 * @param parameters A map of parameters to include in the header.
	 * @return The computed WWW-Authenticate header value.
	 */
	public static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
		StringBuilder wwwAuthenticate = new StringBuilder();
		wwwAuthenticate.append("Bearer");
		if (parameters != null && !parameters.isEmpty()) {
			wwwAuthenticate.append(" ");
			int i = 0;
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				wwwAuthenticate.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
				if (i != parameters.size() - 1) {
					wwwAuthenticate.append(", ");
				}
				i++;
			}
		}
		return wwwAuthenticate.toString();
	}
}
