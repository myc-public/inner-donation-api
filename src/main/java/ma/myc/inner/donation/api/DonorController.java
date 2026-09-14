package ma.myc.inner.donation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ma.myc.inner.donation.domain.dto.CreateDonorRequest;
import ma.myc.inner.donation.domain.dto.DonorResponse;
import ma.myc.inner.donation.domain.dto.UpdateDonorRequest;
import ma.myc.inner.donation.service.DonorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/donors")
@Tag(name = "Donors", description = "Donor management")
public class DonorController {

    private static final Logger log = LoggerFactory.getLogger(DonorController.class);

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a donor")
    public DonorResponse create(@Valid @RequestBody CreateDonorRequest request) {
        log.info("HTTP POST /donors email={}", request.email());
        return donorService.create(request);
    }

    @GetMapping("/{donorId}")
    @Operation(summary = "Get a donor by id")
    public DonorResponse get(@PathVariable UUID donorId) {
        log.info("HTTP GET /donors/{}", donorId);
        return donorService.get(donorId);
    }

    @GetMapping
    @Operation(summary = "List donors (paginated)")
    public Page<DonorResponse> list(
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("HTTP GET /donors page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return donorService.list(pageable);
    }

    @PatchMapping("/{donorId}")
    @Operation(summary = "Update a donor (partial)")
    public DonorResponse update(@PathVariable UUID donorId, @Valid @RequestBody UpdateDonorRequest request) {
        log.info("HTTP PATCH /donors/{}", donorId);
        return donorService.update(donorId, request);
    }

    @DeleteMapping("/{donorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a donor")
    public void delete(@PathVariable UUID donorId) {
        log.info("HTTP DELETE /donors/{}", donorId);
        donorService.delete(donorId);
    }
}