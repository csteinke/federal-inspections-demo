package org.springframework.samples.inspection.system;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

@Configuration
public class WebConfig {

	@Bean
	public LocaleResolver localeResolver() {
		FixedLocaleResolver resolver = new FixedLocaleResolver();
		resolver.setDefaultLocale(Locale.ENGLISH);
		return resolver;
	}

}
