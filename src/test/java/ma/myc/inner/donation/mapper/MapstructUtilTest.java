package ma.myc.inner.donation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MapstructUtilTest {

	@Test
	void testServiceTrimValue() {
		var txt1 = MapstructUtil.trimValue(" Value with multispace    ");
		var expected = "Value with multispace";
		assertThat(txt1)
				.overridingErrorMessage("The expected value should be [%s] but [%s]", expected, txt1)
				.isEqualTo(expected);
	}
}
