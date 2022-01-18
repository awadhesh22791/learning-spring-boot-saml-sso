package com.awadhesh22791;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
@Component
@Log4j2
public class SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		if(authentication.getPrincipal() instanceof DefaultSaml2AuthenticatedPrincipal) {
			String registrationId=((DefaultSaml2AuthenticatedPrincipal)authentication.getPrincipal()).getRelyingPartyRegistrationId();
			ObjectMapper mapper = new ObjectMapper();
			try {
				SsoDetails details = mapper.readValue(ResourceUtils.getFile("classpath:sso.json"), SsoDetails.class);
				if(details!=null) {
					Optional<Map<String, String>> relyingParty = details.getRelyingParties().stream().filter(r->registrationId.equalsIgnoreCase(r.get("registration-id"))).findFirst();
					if(relyingParty.isPresent()) {
						RedirectStrategy redirectStrategy=new DefaultRedirectStrategy();
						redirectStrategy.sendRedirect(request, response, relyingParty.get().get("redirect-url"));
					}
				}
			} catch (IOException e) {
				log.error("Error reading sso details.",e);
			}
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}

}
