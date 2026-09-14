package ma.myc.inner.donation.repository;

import java.math.BigDecimal;
import java.util.List;

import ma.myc.inner.donation.domain.data.City;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Repository
@RequiredArgsConstructor
public class VilleRepository {

	private final JdbcTemplate jdbcTemplate;
	
	@SneakyThrows
	public List<City> findAllByPaysCode(final String paysCode) {
		//TODO a remplacer par acces à la donnees en base
		List<City> cities  = List.of(
                        City.builder().codeCity(new BigDecimal(50)).nameCity("AL HOCEIMA").build(),
				City.builder().codeCity(new BigDecimal(621)).nameCity("BERRECHID").build(),
				City.builder().codeCity(new BigDecimal(614)).nameCity("BEJAAD").build(),
				City.builder().codeCity(new BigDecimal(10)).nameCity("Agadir").build(),
				City.builder().codeCity(new BigDecimal(952)).nameCity("BOUSKOURA").build(),
				City.builder().codeCity(new BigDecimal(780)).nameCity("CASABLANCA")
                                .build());
		return cities;


	}

;}
