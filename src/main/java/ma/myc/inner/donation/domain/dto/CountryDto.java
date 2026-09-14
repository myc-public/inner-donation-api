package ma.myc.inner.donation.domain.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class CountryDto {
   private String nom;
   private int population;
   private String continent;

}
