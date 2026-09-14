package ma.myc.inner.donation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

	@Test
	void computeWWWAuthenticateHeaderValue_withParameters() {
		// Given
		Map<String, String> parameters = new HashMap<>();
		parameters.put("realm", "example");
		parameters.put("error", "invalid_token");
		// When
		String result = SecurityUtils.computeWWWAuthenticateHeaderValue(parameters);
		// Then
		String expected = "Bearer realm=\"example\", error=\"invalid_token\"";
		assertEquals(expected, result);
	}
	
	 @Test
	    void computeWWWAuthenticateHeaderValue_emptyParameters() {
	        // Given
	        Map<String, String> parameters = Collections.emptyMap();
	        // When
	        String result = SecurityUtils.computeWWWAuthenticateHeaderValue(parameters);
	        // Then
	        String expected = "Bearer";
	        assertEquals(expected, result);
	    }

	    @Test
	    void computeWWWAuthenticateHeaderValue_nullParameters() {
	        // Given
	        Map<String, String> parameters = null;
	        // When
	        String result = SecurityUtils.computeWWWAuthenticateHeaderValue(parameters);
	        // Then
	        String expected = "Bearer";
	        assertEquals(expected, result);
	    }

}
