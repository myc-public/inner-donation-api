package ma.myc.inner.donation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import ma.myc.inner.donation.mapper.VilleMapper;
import ma.myc.inner.donation.repository.VilleRepository;
import ma.myc.inner.donation.util.DataGenerator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VilleServiceTest {
	@Mock
	private VilleRepository villeRepository;
	@Mock
	private VilleMapper villeMapper;

	@InjectMocks
	private VilleService villeService;

	@BeforeEach
    void initMockServices() {
        when(villeRepository.findAllByPaysCode(anyString()))
                .thenReturn(DataGenerator.villesAs400Response());
        when(villeMapper.mapToVilleResponse(anyList()))
                .thenReturn(DataGenerator.villesResponse());
        when(villeMapper.mapToVilleResponse(DataGenerator.villeAddRequest()))
                .thenReturn(DataGenerator.getAddedVille());
    }

	@Test
	@DisplayName("Test Service Get List City By paysCode")
	void testServiceGetListVilleByPpayscode() throws Exception {
		assertThat(villeService.getAllVilleByPaysCode(anyString())).hasSize(6);
	}

	@Test
	@DisplayName("Test Service add new City")
	void testServiceAddNewVille() {
		var villeInput = DataGenerator.villeAddRequest();
		assertThat(villeService.addNewVille(villeInput).code())
				.isEqualTo(DataGenerator.getAddedVille().code());
	}

}
