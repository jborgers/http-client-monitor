package com.jpinpoint.monitor;

public interface HttpClientRequestConfigMXBean {
    String getName();

    boolean isExpectContinueEnabled();

    String getProxy();

    String getLocalAddress();

    String getCookieSpec();

    boolean isRedirectsEnabled();

    boolean isRelativeRedirectsAllowed();

    boolean isCircularRedirectsAllowed();

    int getMaxRedirects();

    boolean isAuthenticationEnabled();

    String getTargetPreferredAuthSchemes();

    String getProxyPreferredAuthSchemes();

    int getConnectionRequestTimeout();

    int getConnectTimeout();

    int getSocketTimeout();

    boolean isContentCompressionEnabled();

    boolean isNormalizeUri();
}
