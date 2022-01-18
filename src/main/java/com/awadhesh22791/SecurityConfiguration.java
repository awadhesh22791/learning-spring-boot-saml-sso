package com.awadhesh22791;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
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

@Configuration
public class SecurityConfiguration {
	
	@Value("${metadata.location}")
	String assertingPartyMetadataLocation;
	
	@Autowired
	private SuccessHandler successHandler;

	@Bean
	public RelyingPartyRegistrationRepository relyingPartyRegistrations() {
		List<RelyingPartyRegistration>registrations=new ArrayList<>();
	    RelyingPartyRegistration registration = RelyingPartyRegistrations
	            .fromMetadataLocation("https://dev-10324309.okta.com/app/exk3kjrai208beFXd5d7/sso/saml/metadata")
	            .registrationId("example1")
	            .build();
	    registrations.add(registration);
	    RelyingPartyRegistration registration2 = RelyingPartyRegistrations
	            .fromMetadataLocation("https://dev-10324309.okta.com/app/exk3kjrai208beFXd5d7/sso/saml/metadata")
	            .registrationId("example2")
	            .build();
	    registrations.add(registration2);
	    return new InMemoryRelyingPartyRegistrationRepository(registrations);
	}
	
	@Bean
	SecurityFilterChain app(HttpSecurity http) throws Exception {
		OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(responseToken -> {
            /*Saml2Authentication authentication = OpenSaml4AuthenticationProvider
                    .createDefaultResponseAuthenticationConverter() 
                    .convert(responseToken);*/
            Assertion assertion = responseToken.getResponse().getAssertions().get(0);
            String username = assertion.getSubject().getNameID().getValue();
            System.out.println(username);
            UserDetails userDetails = null;//this.userDetailsService.loadUserByUsername(username); 
            //return MySaml2Authentication(userDetails, authentication);
            return new Saml2Authentication(null, username, null);
        });
		// @formatter:off
		http
			.authorizeHttpRequests((authorize) -> authorize
				.antMatchers("/login/saml2/sso/**/*").permitAll()
			)
			.saml2Login((saml2) -> saml2.relyingPartyRegistrationRepository(relyingPartyRegistrations())
					/*.loginProcessingUrl("/login/saml2/sso")*/
					//.authenticationManager(new ProviderManager(authenticationProvider))
					.successHandler(successHandler))
			.saml2Logout(Customizer.withDefaults());
		// @formatter:on Saml2WebSsoAuthenticationFilter

		return http.build();
	}

	@Bean
	RelyingPartyRegistrationResolver relyingPartyRegistrationResolver(
			RelyingPartyRegistrationRepository registrations) {
		return new DefaultRelyingPartyRegistrationResolver((id) -> registrations.findByRegistrationId(id));
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