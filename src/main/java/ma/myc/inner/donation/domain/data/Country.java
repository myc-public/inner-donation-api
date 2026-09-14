package ma.myc.inner.donation.domain.data;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Country {
    private String code;
    private String name;
    private String continentCode;
    private int population;
}
