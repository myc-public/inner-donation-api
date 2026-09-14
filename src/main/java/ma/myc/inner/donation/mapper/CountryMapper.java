package ma.myc.inner.donation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = MapstructUtil.class)
public interface CountryMapper {

    /**
    @Mapping(source = "nom", target = "name", qualifiedByName = "trim")
    @Mapping(source = "population", target = "population", qualifiedByName = "trim")
    @Mapping(source = "continent", target = "continentCode", qualifiedByName = "trim")
    public Country mapTocountry(CountryDto dto);

    @Mapping(source = "name", target = "nom", qualifiedByName = "trim")
    @Mapping(source = "continentCode", target = "continent", qualifiedByName = "trim")
    public CountryDto mapToDto(Country country);

**/
}
