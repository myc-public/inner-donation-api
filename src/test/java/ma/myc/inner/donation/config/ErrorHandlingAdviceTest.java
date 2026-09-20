package ma.myc.inner.donation.config;

import ma.myc.inner.donation.api.DonorController;
import ma.myc.inner.donation.exception.NotFoundException;
import ma.myc.inner.donation.service.DonorService;
import ma.myc.inner.donation.util.constants.ErrorConstants;
import ma.myc.inner.donation.util.constants.GlobalConstants;
import org.junit.jupiter.api.DisplayName;
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

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DonorController.class)
@Import(TestConfig.class)
@ExtendWith(SpringExtension.class)
class ErrorHandlingAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DonorService donorService;

    @Test
    @WithMockUser(authorities = {GlobalConstants.SCOPE})
    @DisplayName("GET /donors/{id} returns 404 when donor does not exist")
    void get_unknownDonor_returns404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(donorService.get(any(UUID.class)))
                .thenThrow(new NotFoundException("Donor not found: " + unknownId));

        mockMvc.perform(get("/api/v1/donors/{id}", unknownId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.type", is(ErrorConstants.URI_NOT_FOUND)));
    }
}
