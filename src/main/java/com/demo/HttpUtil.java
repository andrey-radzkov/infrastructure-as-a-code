package com.demo;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpUtil {
    public static final int RELAX = 5000;
    public static EasyJson executeHttp(String url) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            return executeRequest(url, httpClient);
        } catch (IOException e) {
            try {
                Thread.sleep(RELAX);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                return executeRequest(url, httpClient);
            } catch (IOException e2) {
                throw new RuntimeException("Error during the api call", e2);
            }
        }
    }

    public static EasyJson executeRequest(String url, CloseableHttpClient httpClient) throws IOException {
        int timeout = 60;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout * 1000)
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .build();
        HttpGet getRequest = new HttpGet(url);
        getRequest.setConfig(defaultRequestConfig);
        CloseableHttpResponse response = httpClient.execute(getRequest);
        String responseString = EntityUtils.toString(response.getEntity(), "utf-8");
        return new EasyJson(responseString);
    }
}
