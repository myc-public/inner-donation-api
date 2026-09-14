package ma.myc.inner.donation.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class MapstructUtil {

    @Named("trim")
    public static String trimValue(String value) {
        return StringUtils.trim(value);
    }
}
