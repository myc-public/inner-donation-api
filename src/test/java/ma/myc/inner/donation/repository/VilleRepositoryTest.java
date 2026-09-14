package ma.myc.inner.donation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import ma.myc.inner.donation.domain.data.City;

@ExtendWith(MockitoExtension.class)
public class VilleRepositoryTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private VilleRepository villeRepository;

	@Test
    @DisplayName("Should return cities for valid country code")
    void Should_Return_Cities_By_Country_Code() throws Exception {
        // Given
//        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class),
//                eq("212")))
//                .thenReturn(DataGenerator.villesAs400Response());

        // When
        List<City> cities = villeRepository.findAllByPaysCode("212");
        // Then
        assertThat(cities).hasSize(6);
        assertThat(cities.get(0).getCodeCity()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(cities.get(0).getNameCity()).isEqualTo("AL HOCEIMA");

        assertThat(cities.get(1).getCodeCity()).isEqualTo(BigDecimal.valueOf(621));
        assertThat(cities.get(1).getNameCity()).isEqualTo("BERRECHID");

        assertThat(cities.get(2).getCodeCity()).isEqualTo(BigDecimal.valueOf(614));
        assertThat(cities.get(2).getNameCity()).isEqualTo("BEJAAD");

        assertThat(cities.get(3).getCodeCity()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(cities.get(3).getNameCity()).isEqualTo("Agadir");

        assertThat(cities.get(4).getCodeCity()).isEqualTo(BigDecimal.valueOf(952));
        assertThat(cities.get(4).getNameCity()).isEqualTo("BOUSKOURA");

        assertThat(cities.get(5).getCodeCity()).isEqualTo(BigDecimal.valueOf(780));
        assertThat(cities.get(5).getNameCity()).isEqualTo("CASABLANCA");

    }
}
