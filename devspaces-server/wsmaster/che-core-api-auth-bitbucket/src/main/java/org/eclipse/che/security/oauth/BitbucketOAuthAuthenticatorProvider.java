/*
 * Copyright (c) 2012-2022 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.security.oauth.shared.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides implementation of BitBucket {@link OAuthAuthenticator} based on available configuration.
 */
@Singleton
public class BitbucketOAuthAuthenticatorProvider implements Provider<OAuthAuthenticator> {
  private static final Logger LOG =
      LoggerFactory.getLogger(BitbucketOAuthAuthenticatorProvider.class);
  private final OAuthAuthenticator authenticator;

  @Inject
  public BitbucketOAuthAuthenticatorProvider(
      @Nullable @Named("che.oauth2.bitbucket.clientid_filepath") String bitbucketClientIdPath,
      @Nullable @Named("che.oauth2.bitbucket.clientsecret_filepath")
          String bitbucketClientSecretPath,
      @Nullable @Named("che.oauth.bitbucket.redirecturis") String[] redirectUris,
      @Nullable @Named("che.oauth.bitbucket.authuri") String authUri,
      @Nullable @Named("che.oauth.bitbucket.tokenuri") String tokenUri)
      throws IOException {
    authenticator =
        getOAuthAuthenticator(
            bitbucketClientIdPath, bitbucketClientSecretPath, redirectUris, authUri, tokenUri);
    LOG.debug("{} Bitbucket OAuth Authenticator is used.", authenticator);
  }

  @Override
  public OAuthAuthenticator get() {
    return authenticator;
  }

  private OAuthAuthenticator getOAuthAuthenticator(
      String clientIdPath,
      String clientSecretPath,
      String[] redirectUris,
      String authUri,
      String tokenUri)
      throws IOException {

    if (!isNullOrEmpty(clientIdPath)
        && !isNullOrEmpty(clientSecretPath)
        && !isNullOrEmpty(authUri)
        && !isNullOrEmpty(tokenUri)
        && Objects.nonNull(redirectUris)
        && redirectUris.length != 0) {
      final String clientId = Files.readString(Path.of(clientIdPath)).trim();
      final String clientSecret = Files.readString(Path.of(clientSecretPath)).trim();
      if (!isNullOrEmpty(clientId) && !isNullOrEmpty(clientSecret)) {
        return new BitbucketOAuthAuthenticator(
            clientId, clientSecret, redirectUris, authUri, tokenUri);
      }
    }
    return new NoopOAuthAuthenticator();
  }

  static class NoopOAuthAuthenticator extends OAuthAuthenticator {
    @Override
    public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
      throw new OAuthAuthenticationException(
          "The fallback noop authenticator cannot be used for Bitbucket authentication. Make sure OAuth is properly configured.");
    }

    @Override
    public String getOAuthProvider() {
      return "Noop";
    }
  }
}
