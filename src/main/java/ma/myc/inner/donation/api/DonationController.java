package ma.myc.inner.donation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ma.myc.inner.donation.domain.dto.CreateDonationRequest;
import ma.myc.inner.donation.domain.dto.DonationResponse;
import ma.myc.inner.donation.domain.dto.UpdateDonationRequest;
import ma.myc.inner.donation.service.DonationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donations")
@Tag(name = "Donations", description = "Donation management")
public class DonationController {

    private static final Logger log = LoggerFactory.getLogger(DonationController.class);

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a donation")
    public DonationResponse create(@Valid @RequestBody CreateDonationRequest request) {
        log.info("HTTP POST /donations donorId={} amount={}", request.donor().donorId(), request.amount());
        return donationService.create(request);
    }

    @GetMapping("/{donationId}")
    @Operation(summary = "Get a donation by id")
    public DonationResponse get(@PathVariable UUID donationId) {
        log.info("HTTP GET /donations/{}", donationId);
        return donationService.get(donationId);
    }

    @GetMapping
    @Operation(summary = "List donations")
    public List<DonationResponse> list() {
        log.info("HTTP GET /donations");
        return donationService.list();
    }

    @GetMapping("/by-donor/{donorId}")
    @Operation(summary = "List donations by donor")
    public List<DonationResponse> listByDonor(@PathVariable UUID donorId) {
        log.info("HTTP GET /donations/by-donor/{}", donorId);
        return donationService.listByDonor(donorId);
    }

    @PatchMapping("/{donationId}")
    @Operation(summary = "Update a donation (partial)")
    public DonationResponse update(@PathVariable UUID donationId, @Valid @RequestBody UpdateDonationRequest request) {
        log.info("HTTP PATCH /donations/{}", donationId);
        return donationService.update(donationId, request);
    }

    @DeleteMapping("/{donationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a donation")
    public void delete(@PathVariable UUID donationId) {
        log.info("HTTP DELETE /donations/{}", donationId);
        donationService.delete(donationId);
    }
}