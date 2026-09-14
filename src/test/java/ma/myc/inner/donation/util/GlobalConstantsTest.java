package ma.myc.inner.donation.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ma.myc.inner.donation.util.constants.GlobalConstants;

@ExtendWith(MockitoExtension.class)
class GlobalConstantsTest {

	@Test
	@DisplayName("Test Constants utility class")
	void testConstantsUtilityClass()
			throws IllegalAccessException, InstantiationException {
		final Class<?> cls = GlobalConstants.class;
		final Constructor<?> c = cls.getDeclaredConstructors()[0];
		c.setAccessible(true);

		Throwable targetException = null;
		try {
			c.newInstance((Object[]) null);
		} catch (InvocationTargetException e) {
			targetException = e.getTargetException();
		}

		assertThat(targetException)
				.overridingErrorMessage("The expected value should not null")
				.isNotNull();

		assertThat(targetException.getClass())
				.overridingErrorMessage("The expected value should be [InstantiationException.class]")
				.isEqualTo(InstantiationException.class);
	}

}
