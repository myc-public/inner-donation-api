package ma.myc.inner.donation.mapper;

import ma.myc.inner.donation.domain.bo.DonorBO;
import ma.myc.inner.donation.domain.dto.CreateDonorRequest;
import ma.myc.inner.donation.domain.dto.DonorResponse;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DonorMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    DonorBO toBo(CreateDonorRequest request);

    @Mapping(target = "donorId", source = "id")
    DonorResponse toResponse(DonorBO bo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget DonorBO target, ma.myc.inner.donation.domain.dto.UpdateDonorRequest request);

    default UUID newId() { return UUID.randomUUID(); }
}