package ma.myc.inner.donation.domain.bo;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donor", uniqueConstraints = @UniqueConstraint(name = "uk_donor_email", columnNames = "email"))

public class DonorBO {

    @Id
    @Column(name = "donor_id", nullable = false, updatable = false)
    private UUID id;

    @Setter
    @Column(name = "last_name", nullable = false, length = 120)
    private String lastName;

    @Setter
    @Column(name = "first_name", nullable = false, length = 120)
    private String firstName;

    @Setter
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Setter
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Setter
    @Column(name = "country", length = 80)
    private String country;

    @Setter
    @Column(name= "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DonorBO() {
    }

    public DonorBO(UUID id, String lastName, String firstName, String email, LocalDate dateOfBirth, String country, Instant createdAt) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.createdAt=createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCountry() {
        return country;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}