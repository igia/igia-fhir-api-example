/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.example.fhir.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.igia.example.fhir.security.oauth2.CachedUserInfoTokenServices;
import io.igia.example.fhir.security.oauth2.SimpleAuthoritiesExtractor;
import io.igia.example.fhir.security.oauth2.SimplePrincipalExtractor;

@Configuration
public class OAuth2TokenServicesConfiguration {

    private static final String OAUTH2_PRINCIPAL_ATTRIBUTE = "preferred_username";
    private static final String OAUTH2_AUTHORITIES_ATTRIBUTE = "roles";

    private final ResourceServerProperties resourceServerProperties;

	public OAuth2TokenServicesConfiguration(ResourceServerProperties resourceServerProperties) {
		this.resourceServerProperties = resourceServerProperties;
	}

    @Bean
    public UserInfoTokenServices userInfoTokenServices(PrincipalExtractor principalExtractor, AuthoritiesExtractor authoritiesExtractor) {
        UserInfoTokenServices userInfoTokenServices =
            new CachedUserInfoTokenServices(resourceServerProperties.getUserInfoUri(), resourceServerProperties.getClientId());

        userInfoTokenServices.setPrincipalExtractor(principalExtractor);
        userInfoTokenServices.setAuthoritiesExtractor(authoritiesExtractor);
        return userInfoTokenServices;
    }
    @Bean
    public PrincipalExtractor principalExtractor() {
        return new SimplePrincipalExtractor(OAUTH2_PRINCIPAL_ATTRIBUTE);
    }

    @Bean
    public AuthoritiesExtractor authoritiesExtractor() {
        return new SimpleAuthoritiesExtractor(OAUTH2_AUTHORITIES_ATTRIBUTE);
    }
}
