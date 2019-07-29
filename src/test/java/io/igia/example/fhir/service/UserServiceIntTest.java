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
package io.igia.example.fhir.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.igia.example.fhir.IgiafhirapiexampleApp;
import io.igia.example.fhir.config.Constants;
import io.igia.example.fhir.domain.User;
import io.igia.example.fhir.repository.UserRepository;
import io.igia.example.fhir.security.AuthoritiesConstants;
import io.igia.example.fhir.service.UserService;
import io.igia.example.fhir.service.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IgiafhirapiexampleApp.class)
@Transactional
public class UserServiceIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditingHandler auditingHandler;

    @Mock
    DateTimeProvider dateTimeProvider;

    private User user;

    @Before
    public void init() {
        user = new User();
        user.setLogin("johndoe");
        user.setActivated(true);
        user.setEmail("johndoe@localhost");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.now()));
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }

    @Test
    @Transactional
    public void assertThatAnonymousUserIsNotGet() {
        user.setId(Constants.ANONYMOUS_USER);
        user.setLogin(Constants.ANONYMOUS_USER);
        if (!userRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent()) {
            userRepository.saveAndFlush(user);
        }
        final PageRequest pageable = PageRequest.of(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.getAllManagedUsers(pageable);
        assertThat(allManagedUsers.getContent().stream()
            .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
            .isTrue();
    }


    @Test
    @Transactional
    public void assertThatUserLocaleIsCorrectlySetFromAuthenticationDetails() {
        user.setId(Constants.ANONYMOUS_USER);
        user.setLogin(Constants.ANONYMOUS_USER);

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("sub", user.getId());
        userDetails.put("preferred_username", user.getLogin());
        userDetails.put("given_name", user.getFirstName());
        userDetails.put("family_name", user.getLastName());
        userDetails.put("email", user.getEmail());
        userDetails.put("picture", user.getImageUrl());
        userDetails.put("locale", "en_US");

        OAuth2Authentication authentication = createMockOAuth2AuthenticationWithDetails(userDetails);

        UserDTO userDTO = userService.getUserFromAuthentication(authentication);

        assertEquals("User langkey is not corretly set.", userDTO.getLangKey(), "en");

        userDetails.put("locale", "it-IT");
        authentication = createMockOAuth2AuthenticationWithDetails(userDetails);

        userDTO = userService.getUserFromAuthentication(authentication);

        assertEquals("User langkey is not corretly set.", userDTO.getLangKey(), "it");
    }

    private OAuth2Authentication createMockOAuth2AuthenticationWithDetails(Map<String, Object> userDetails) {
        Set<String> scopes = new HashSet<String>();

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(Constants.ANONYMOUS_USER, Constants.ANONYMOUS_USER, authorities);
        usernamePasswordAuthenticationToken.setDetails(userDetails);

        OAuth2Request authRequest = new OAuth2Request(null, "testClient", null, true, scopes, null, null, null, null);

        return new OAuth2Authentication(authRequest, usernamePasswordAuthenticationToken);
    }
}
