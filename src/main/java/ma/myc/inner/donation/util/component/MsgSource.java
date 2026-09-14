package ma.myc.inner.donation.util.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MsgSource {

	@Autowired
	private MessageSource source;

	public String getMessage(String code, Object... args) {
		var locale = LocaleContextHolder.getLocale();
		return source.getMessage(code, args, "!" + code + "!", locale);
	}

}
