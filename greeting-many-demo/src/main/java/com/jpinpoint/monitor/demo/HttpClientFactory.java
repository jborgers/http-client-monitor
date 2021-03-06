package com.jpinpoint.monitor.demo;

import com.jpinpoint.monitor.HttpClientMonitorUtil;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class HttpClientFactory {
    private static final int MAX_CONN_TOTAL = 6;
    private static final int MAX_CONN_PER_ROUTE = 4;
    private static final int DEF_MAX_CONN_PER_ROUTE = 3;
    private static final int CONNECTION_TIMEOUT_MILLISEC = 250;
    private static final int CONNECTION_REQUEST_TIMEOUT_MILLISEC = 2000;
    private static final int SO_TIMEOUT_MILLISEC_1 = 5000;
    private static final int SO_TIMEOUT_MILLISEC_2 = 50;

    public RestTemplate createRestTemplate(String serviceName) {
        ClientHttpRequestFactory factory = getClientHttpRequestFactory(serviceName);

        return new RestTemplate(factory);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(String serviceName) {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();

        httpComponentsClientHttpRequestFactory.setReadTimeout(SO_TIMEOUT_MILLISEC_1);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(CONNECTION_TIMEOUT_MILLISEC);
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLISEC);
        HttpClient httpClient = createHttpClient(serviceName);
        HttpClientMonitorUtil.registerRequestConfigMBean(serviceName, httpClient);
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        return httpComponentsClientHttpRequestFactory;
    }

    private HttpClient createHttpClient(String serviceName) {
        RequestConfig requestConfig = createRequestConfig();
        PoolingHttpClientConnectionManager connectionMgr = createConnectionManager();

        HttpClientMonitorUtil.registerPoolStatsMBean(serviceName, connectionMgr);

        HttpClientMonitorUtil.logPoolInfo(serviceName, "start");
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionMgr)
                //.addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                //.setKeepAliveStrategy(keepConnectionAliveStrategy)
                .disableConnectionState()
                .build();
    }

    private RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLISEC)
                .setConnectTimeout(CONNECTION_TIMEOUT_MILLISEC)
                .setSocketTimeout(SO_TIMEOUT_MILLISEC_1) // overrides setting in SocketConfig
                .build();
    }

    private PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setDefaultMaxPerRoute(DEF_MAX_CONN_PER_ROUTE);
        connectionManager.setMaxTotal(MAX_CONN_TOTAL);

        //HttpHost host = new HttpHost(HOST_URL); // HOST_URL = "localhost:8081"; // wrong! bug
        HttpHost host = new HttpHost("localhost", 8081, "http");
        HttpRoute route = new HttpRoute(host);

        SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(SO_TIMEOUT_MILLISEC_2) // timeout gets overridden by requestConfig
                .setSoKeepAlive(true)
                .setTcpNoDelay(false)
                .build();
        connectionManager.setSocketConfig(route.getTargetHost(), sc);

        connectionManager.setMaxPerRoute(route, MAX_CONN_PER_ROUTE);

        log.debug("For host {} name {}, targetHost {} name {} and route {} : created socketConfig:{} gotten socketConfig:{}", host, host.getHostName(), route.getTargetHost(), route.getTargetHost().getHostName(), route, sc, connectionManager.getSocketConfig(host));

        return connectionManager;
    }


}

