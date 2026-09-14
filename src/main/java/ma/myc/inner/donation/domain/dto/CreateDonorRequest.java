package ma.myc.inner.donation.domain.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

public record CreateDonorRequest(
        @NotBlank @Size(max = 120) String lastName,
        @NotBlank @Size(max = 120) String firstName,
        @NotBlank @Email @Size(max = 320) String email,
        LocalDate dateOfBirth,
        @Size(max = 80) String country
) {


}