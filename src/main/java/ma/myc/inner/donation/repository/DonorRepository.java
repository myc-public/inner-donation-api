package ma.myc.inner.donation.repository;

import ma.myc.inner.donation.domain.bo.DonorBO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DonorRepository extends JpaRepository<DonorBO, UUID> {
    Optional<DonorBO> findByEmail(String email);
    boolean existsByEmail(String email);
}