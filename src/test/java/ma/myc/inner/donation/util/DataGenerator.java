package ma.myc.inner.donation.util;

import java.math.BigDecimal;
import java.util.List;

import lombok.experimental.UtilityClass;
import ma.myc.inner.donation.domain.data.City;
import ma.myc.inner.donation.domain.dto.VilleAddRequest;
import ma.myc.inner.donation.domain.dto.VilleResponse;

@UtilityClass
public class DataGenerator {

	public static List<VilleResponse> villesResponse() {
		return List.of(
				VilleResponse.builder().code("50").label("AL HOCEIMA").build(),
				VilleResponse.builder().code("621").label("BERRECHID").build(),
				VilleResponse.builder().code("614").label("BEJAAD").build(),
				VilleResponse.builder().code("10").label("Agadir").build(),
				VilleResponse.builder().code("952").label("BOUSKOURA").build(),
				VilleResponse.builder().code("780").label("CASABLANCA")
						.build());
	}

	public static VilleAddRequest villeAddRequest() {
		return VilleAddRequest.builder().code("614").active(true).label("AGADIR").build();
	}

	public static VilleResponse getAddedVille() {
		return VilleResponse.builder().code("614").active(true).label("AGADIR").build();
	}

	public static List<City> villesAs400Response() {
		return List.of(
				City.builder().codeCity(new BigDecimal(50))
						.nameCity("AL HOCEIMA").build(),
				City.builder().codeCity(new BigDecimal(621))
						.nameCity("BERRECHID").build(),
				City.builder().codeCity(new BigDecimal(614))
						.nameCity("BEJAAD").build(),
				City.builder().codeCity(new BigDecimal(10)).nameCity("Agadir")
						.build(),
				City.builder().codeCity(new BigDecimal(952))
						.nameCity("BOUSKOURA").build(),
				City.builder().codeCity(new BigDecimal(780))
						.nameCity("CASABLANCA").build());
	}
}
