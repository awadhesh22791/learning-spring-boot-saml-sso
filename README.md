# Learning Spring Boot SAML based SSO
Following are the points to update:
 1. add idp details in sso.json
 2. use http://localhost:8080/login to get list of available idp's to authenticate
 3. use http://localhost:8080/saml2/authenticate/{registration-id} to directly authenticate user
 4. SingleSignOn url: http://localhost:8080/login/saml2/sso/{registration-id}, Recipient URL: http://localhost:8080/login/saml2/sso/{registration-id}, Destination URL: http://localhost:8080/login/saml2/sso/{registration-id} and Audience url: http://localhost:8080/saml2/service-provider-metadata/{registration-id}. These details will be use on okta developer console.
