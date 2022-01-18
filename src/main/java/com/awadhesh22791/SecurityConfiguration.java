package com.awadhesh22791;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SecurityConfiguration {
	
	@Bean
	public RelyingPartyRegistrationRepository relyingPartyRegistrations() {
		ObjectMapper mapper=new ObjectMapper();
		List<RelyingPartyRegistration>registrations=new ArrayList<>();
		try {
			SsoDetails details = mapper.readValue(ResourceUtils.getFile("classpath:sso.json"), SsoDetails.class);
			if(details!=null) {
				details.getRelyingParties().stream().forEach(relyingParty->{
					RelyingPartyRegistration registration = RelyingPartyRegistrations
				            .fromMetadataLocation(relyingParty.get("metadata"))
				            .registrationId(relyingParty.get("registration-id"))
				            .build();
					registrations.add(registration);
				});
			}
		} catch (IOException e) {
			log.error("Error reading sso details.",e);
		}
	    return new InMemoryRelyingPartyRegistrationRepository(registrations);
	}
	
	@Bean
	SecurityFilterChain app(HttpSecurity http,SuccessHandler successHandler,FailureHandler failureHandler) throws Exception {
		// @formatter:off
		http
			.authorizeHttpRequests(authorize -> authorize
				.antMatchers("/login/saml2/sso/**/*").permitAll()
			)
			.saml2Login(saml2 -> saml2.relyingPartyRegistrationRepository(relyingPartyRegistrations())
					/*.loginProcessingUrl("/login/saml2/sso")*/
					//.authenticationManager(new ProviderManager(authenticationProvider))
					.successHandler(successHandler)
					.failureHandler(failureHandler))
			.saml2Logout(Customizer.withDefaults());
		// @formatter:on Saml2WebSsoAuthenticationFilter

		return http.build();
	}

	@Bean
	RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
			RelyingPartyRegistrationRepository registrations) {
		return new DefaultRelyingPartyRegistrationResolver(registrations::findByRegistrationId);
	}

	@Bean
	Saml2AuthenticationTokenConverter authentication(RelyingPartyRegistrationResolver registrations) {
		return new Saml2AuthenticationTokenConverter(registrations);
	}

	@Bean
	FilterRegistrationBean<Saml2MetadataFilter> metadata(RelyingPartyRegistrationResolver registrations) {
		Saml2MetadataFilter metadata = new Saml2MetadataFilter(registrations, new OpenSamlMetadataResolver());
		FilterRegistrationBean<Saml2MetadataFilter> filter = new FilterRegistrationBean<>(metadata);
		filter.setOrder(-101);
		return filter;
	}

}