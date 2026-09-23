package ma.myc.inner.donation.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import tools.jackson.databind.json.JsonMapper;

import ma.myc.inner.donation.config.TestConfig;
import ma.myc.inner.donation.domain.dto.VilleAddRequest;
import ma.myc.inner.donation.service.VilleService;
import ma.myc.inner.donation.util.DataGenerator;
import ma.myc.inner.donation.util.constants.ErrorConstants;
import ma.myc.inner.donation.util.constants.GlobalConstants;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(VilleAPI.class)
@Import({ TestConfig.class })
@ExtendWith(SpringExtension.class)
class VilleApiTest {
	private static final String VILLES_RESOURCE = VilleAPI.BASE_URL;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VilleService villeService;

	private static final JsonMapper om = new JsonMapper();

	@BeforeEach
    void initMockServices() throws Exception {
        when(villeService.getAllVilleByPaysCode(anyString())).thenReturn(DataGenerator.villesResponse());
        when(villeService.addNewVille(org.mockito.ArgumentMatchers.any(VilleAddRequest.class))).thenReturn(DataGenerator.getAddedVille());
    }

	@Test
	@WithMockUser(authorities = { GlobalConstants.SCOPE })
	void getAllVilles_return_success_with_sizeEq6() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(VILLES_RESOURCE + "?pays_code=212")
				.accept(MediaType.APPLICATION_JSON)
				.characterEncoding("UTF-8");

		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(6)))
				.andExpect(jsonPath("$[2].code", is("614")));
	}

	@Test
	@WithMockUser(authorities = { GlobalConstants.SCOPE })
	void getAllVilles_return_error_when_paycode_missing() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(VILLES_RESOURCE)
				.accept(MediaType.APPLICATION_JSON)
				.characterEncoding("UTF-8");

		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.type", is(ErrorConstants.URI_MISSING_REQUEST_PARAMETER)));
	}

	@Test
	@WithMockUser(authorities = { GlobalConstants.SCOPE })
	void addNewVille_return_success_with_NotEmptyCode() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post(VILLES_RESOURCE)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(om.writeValueAsString(DataGenerator.villeAddRequest()))
				.characterEncoding("UTF-8")
				.with(csrf());

		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code", is("614")));
	}
}
