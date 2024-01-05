package com.williamwigemo;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public abstract class OAuthHandler<T extends Throwable> {
    private String code;
    private CountDownLatch codeLatch;
    private final OAuthCredentialsManager credentialsManager;
    private final String serviceName;
    private OAuthContexts oAuthContexts;

    private final Logger logger;

    public <R> OAuthHandler(String serviceName, Logger logger, Class<R> cls) {
        this.code = null;
        this.codeLatch = new CountDownLatch(1);
        this.logger = logger;
        this.credentialsManager = new OAuthCredentialsManager(cls);
        this.serviceName = serviceName;
    }

    public void logout() {
        this.logger.info(String.format("Logging out from %s", this.serviceName));
        this.credentialsManager.clearOAuthCredentials();
        this.accessToken = null;
    }

    private String accessToken = null;

    public String getAccessToken() throws T {
        if (!this.credentialsManager.hasValidAccessToken() && this.credentialsManager.getRefreshToken() != null) {
            this.logger.fine(String.format("%s access token has expired. Refreshing access token with refresh token",
                    this.serviceName));

            this.credentialsManager.saveOAuthCredentials(this.getRefreshedAccessToken());
            this.accessToken = this.credentialsManager.getAccessToken();
        } else if (accessToken == null) {
            this.accessToken = this.credentialsManager.getAccessToken();
        }

        return accessToken;
    }

    public void setOAuthContexts(OAuthContexts oAuthContexts) {
        this.oAuthContexts = oAuthContexts;
    }

    public OAuthCredentialsManager getCredentialsManager() {
        return credentialsManager;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.codeLatch.countDown();
    }

    public CountDownLatch getCodeLatch() {
        return codeLatch;
    }

    public void setCodeLatch(CountDownLatch codeLatch) {
        this.codeLatch = codeLatch;
    }

    public abstract String createAuthorizeUrl();

    public abstract OAuthCredentialsResponse fetchAccessTokenFromCode() throws T;

    public abstract OAuthCredentialsResponse getRefreshedAccessToken() throws T;

    public void authorize() throws T, IOException {
        if (this.credentialsManager.hasValidAccessToken()) {
            this.logger.info(String.format("%s is already authorized", this.getClass().getSimpleName()));
            return;
        } else if (this.credentialsManager.getRefreshToken() != null) {
            try {
                this.credentialsManager.saveOAuthCredentials(this.getRefreshedAccessToken());
                return;
            } catch (Exception e) {
                this.logger.warning(String.format("Failed to refresh access token for %s", this.serviceName));
            }
        }

        this.setCodeLatch(new CountDownLatch(1));

        OAuthHttpServer httpServer = OAuthHttpServer.getInstance();

        if (this.oAuthContexts == null) {
            throw new IllegalArgumentException(String.format("oAuthContexts must be non-null"));
        }
        httpServer.start(this.oAuthContexts);

        System.out.println(String.format("Authenticate to %s by following this link: %s", this.serviceName,
                this.createAuthorizeUrl()));

        try {
            this.getCodeLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } finally {
            httpServer.stop();
        }

        this.credentialsManager.saveOAuthCredentials(this.fetchAccessTokenFromCode());
    }
}
