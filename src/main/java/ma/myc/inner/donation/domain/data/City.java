package ma.myc.inner.donation.domain.data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {

	private String nameCity;
	private BigDecimal codeCity;
}
