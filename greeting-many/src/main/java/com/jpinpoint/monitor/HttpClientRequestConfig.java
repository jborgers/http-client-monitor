package com.jpinpoint.monitor;

import org.apache.http.client.config.RequestConfig;

import java.util.Objects;

public class HttpClientRequestConfig implements HttpClientRequestConfigMXBean {

    private final RequestConfig reqConfig;

    @Override
    public String getName() {
        return "HttpClientRequestConfig";
    }

    public HttpClientRequestConfig(RequestConfig rc) {
        reqConfig = rc;
    }

    @Override
    public boolean isExpectContinueEnabled() {
        return reqConfig.isExpectContinueEnabled();
    }

    @Override
    public String getProxy() {
        return Objects.toString(reqConfig.getProxy());
    }

    @Override
    public String getLocalAddress() {
        return Objects.toString(reqConfig.getLocalAddress());
    }

    @Override
    public String getCookieSpec() {
        return reqConfig.getCookieSpec();
    }

    @Override
    public boolean isRedirectsEnabled() {
        return reqConfig.isRedirectsEnabled();
    }

    @Override
    public boolean isRelativeRedirectsAllowed() {
        return reqConfig.isRelativeRedirectsAllowed();
    }

    @Override
    public boolean isCircularRedirectsAllowed() {
        return reqConfig.isCircularRedirectsAllowed();
    }

    @Override
    public int getMaxRedirects() {
        return reqConfig.getMaxRedirects();
    }

    @Override
    public boolean isAuthenticationEnabled() {
        return reqConfig.isAuthenticationEnabled();
    }

    @Override
    public String getTargetPreferredAuthSchemes() {
        return Objects.toString(reqConfig.getTargetPreferredAuthSchemes());
    }

    @Override
    public String getProxyPreferredAuthSchemes() {
        return Objects.toString(reqConfig.getProxyPreferredAuthSchemes());
    }

    @Override
    public int getConnectionRequestTimeout() {
        return reqConfig.getConnectionRequestTimeout();
    }

    @Override
    public int getConnectTimeout() {
        return reqConfig.getConnectTimeout();
    }

    @Override
    public int getSocketTimeout() {
        return reqConfig.getSocketTimeout();
    }


    @Override
    public boolean isContentCompressionEnabled() {
        return reqConfig.isContentCompressionEnabled();
    }

    @Override
    public boolean isNormalizeUri() {
        return reqConfig.isNormalizeUri();
    }

    public String toString() {
        return reqConfig.toString();
    }

}
