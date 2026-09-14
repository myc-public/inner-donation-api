package ma.myc.inner.donation.mapper;

import java.util.List;

import ma.myc.inner.donation.domain.data.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ma.myc.inner.donation.domain.dto.VilleAddRequest;
import ma.myc.inner.donation.domain.dto.VilleResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = MapstructUtil.class)
public interface VilleMapper {

	@Mapping(source = "nameCity", target = "label", qualifiedByName = "trim")
	@Mapping(source = "codeCity", target = "code")
	@Mapping(target = "active", constant = "true")
	public VilleResponse mapToVilleResponse(City city);
	
	public List<VilleResponse> mapToVilleResponse(List<City> cities);
	
	public VilleResponse mapToVilleResponse(VilleAddRequest request);


}
