package ma.myc.inner.donation.mapper;

import ma.myc.inner.donation.domain.bo.DonationBO;
import ma.myc.inner.donation.domain.bo.DonorBO;
import ma.myc.inner.donation.domain.dto.CreateDonationRequest;
import ma.myc.inner.donation.domain.dto.DonationResponse;
import ma.myc.inner.donation.domain.dto.UpdateDonationRequest;
import org.mapstruct.*;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DonationMapper {

    @Mapping(target = "id", expression = "java(newId())")
    @Mapping(target = "timestamp", expression = "java(now())")
    @Mapping(target = "donorId", source = "donor.donorId")
    DonationBO toBo(CreateDonationRequest request);

    @Mapping(target = "donationId", source = "id")
    @Mapping(target = "donorId", source = "donorId")
    DonationResponse toResponse(DonationBO bo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "type", expression = "java(request.type() != null ? request.type() : target.isType())")
    void patch(@MappingTarget DonationBO target, UpdateDonationRequest request);

    default UUID newId() { return UUID.randomUUID(); }
    default Instant now() { return Instant.now(); }
}