package ma.myc.inner.donation.service;

import ma.myc.inner.donation.domain.dto.*;

import java.util.List;
import java.util.UUID;

public interface DonationService {
    DonationResponse create(CreateDonationRequest request);
    DonationResponse get(UUID donationId);
    List<DonationResponse> list();
    List<DonationResponse> listByDonor(UUID donorId);
    DonationResponse update(UUID donationId, UpdateDonationRequest request);
    void delete(UUID donationId);
}