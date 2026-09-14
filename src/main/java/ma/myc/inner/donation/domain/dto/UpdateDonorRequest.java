package ma.myc.inner.donation.domain.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateDonorRequest(
        @Size(max = 120) String lastName,
        @Size(max = 120) String firstName,
        @Email @Size(max = 320) String email,
        LocalDate dateOfBirth,
        @Size(max = 80) String country
) {}