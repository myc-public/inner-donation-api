package ma.myc.inner.donation.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import ma.myc.inner.donation.domain.dto.VilleAddRequest;
import ma.myc.inner.donation.domain.dto.VilleResponse;
import ma.myc.inner.donation.service.VilleService;
import ma.myc.inner.donation.util.constants.ErrorConstants;
import ma.myc.inner.donation.util.constants.GlobalConstants;

@Tag(name = GlobalConstants.DONATION_APIS_TAG)
@RestController
@RequestMapping(VilleAPI.BASE_URL)
@RequiredArgsConstructor
public class VilleAPI {
	public static final String BASE_URL = "/v1/geo/villes";

	private final VilleService villeService;

	@Operation(summary = "Retreive all Villes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List villes", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = VilleResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
			@ApiResponse(responseCode = "404", description = "Villes not found", content = @Content) })
	@GetMapping(produces = { "application/json" })
	public ResponseEntity<List<VilleResponse>> getAllVilles(
			@NotEmpty(message = ErrorConstants.ERR_CODE_PAYS_NOTEMPTY) @RequestParam(value = "pays_code", required = true) String paysCode) {
		return new ResponseEntity<>(
				villeService.getAllVilleByPaysCode(paysCode), HttpStatus.OK);
	}

	@Operation(summary = "Add new City")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "City added", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = VilleResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content) })
	@PostMapping(produces = { "application/json" })
	public ResponseEntity<VilleResponse> getAllVilles(@Valid @RequestBody VilleAddRequest villeAddRequest) {
		return new ResponseEntity<>(villeService.addNewVille(villeAddRequest), HttpStatus.OK);
	}

}
