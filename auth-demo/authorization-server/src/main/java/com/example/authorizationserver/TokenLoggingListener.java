package com.example.authorizationserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TokenLoggingListener {

    private static final Logger logger = LoggerFactory.getLogger(TokenLoggingListener.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();

        // Check if this is the specific token response event
        if (auth instanceof OAuth2AccessTokenAuthenticationToken accessTokenAuth) {

            String accessToken = accessTokenAuth.getAccessToken().getTokenValue();
            String refreshToken = (accessTokenAuth.getRefreshToken() != null)
                    ? accessTokenAuth.getRefreshToken().getTokenValue()
                    : "None issued";

            logger.info("--- TOKEN ISSUED ---");
            logger.info("Access Token: {}", accessToken);
            logger.info("Refresh Token: {}", refreshToken);
            logger.info("--------------------");
        }
    }
}