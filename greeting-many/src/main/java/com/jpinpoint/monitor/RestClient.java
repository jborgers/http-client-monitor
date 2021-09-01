package com.jpinpoint.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.*;

@Slf4j
public class RestClient {
    public static final String LOG_REQUEST_TO_ENDPOINT = "Request to endpoint: {} failed with code: {} and message {}";
    public static final String SERVICE_CONNECTION_MSG = "Service connection issue after %s ms, serviceName=%s, exception-message=%s";
    private static final String serviceName = "hello service";
    private final HttpClientFactory httpClientFactory = new HttpClientFactory();
    private final RestTemplate restTemplate = httpClientFactory.createRestTemplate();
    private final ExecutorService executor = Executors.newFixedThreadPool(20);

    public String callManyHellos(String threads, String port) {
        int numThreads = Integer.parseInt(threads);
        for (int i = 0; i < numThreads - 1; i++) {
            executor.submit(() -> {
                return doGet(port);
            });
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
        return doGet(port);
    }

    private String doGet(String port) {
        String value;
        String uri = "http://localhost:" + port + "/hello";
        URI uriUrl = createUri(uri);
        ResponseEntity<String> response;
        long start = System.currentTimeMillis();

        try {
            httpClientFactory.logPoolInfo("before Hello service call");

            response = restTemplate.exchange(uriUrl, HttpMethod.GET, new HttpEntity<>(null), String.class);
            //assertHttpCallResponseIsValid(response, getUri());
            value = retrieveHttpCallResponse(response);

        } catch(ResourceAccessException e) {
            throw new RuntimeException(String.format(SERVICE_CONNECTION_MSG, System.currentTimeMillis() - start, serviceName, e.getMessage()), e);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.debug(LOG_REQUEST_TO_ENDPOINT, uri, ex.getRawStatusCode(), ex.getMessage());
            throw new RuntimeException(String.format(SERVICE_CONNECTION_MSG, System.currentTimeMillis() - start, serviceName, ex.getMessage()), ex);
        } catch (RestClientException | IllegalArgumentException e) {
            throw new RuntimeException(String.format("The retrieval of a value from the endpoint with uriUrl %s failed.", uriUrl), e);
        }
        finally {
            httpClientFactory.logPoolInfo("after Hello service call");
        }
        return value;
    }

    protected URI createUri(String uri) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(uri);
        return uriComponentsBuilder.buildAndExpand(Collections.emptyMap()).toUri();
    }

    private String retrieveHttpCallResponse(ResponseEntity<String> response) {
        String value = null;
        if (response.hasBody()) {
            value = response.getBody();
        } else {
            value = response.toString();
        }
        return value;
    }


}
