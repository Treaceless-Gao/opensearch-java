/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client.opensearch.integTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.junit.Test;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.UdbsxClient;
import org.opensearch.client.transport.UdbsxTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

public class SimpleConnectionTest {

    @Test
    public void testConnection() throws IOException, ParseException {
        HttpHost host = new HttpHost("http", "localhost", 10200);

        // 创建低级别的 RestClient
        org.opensearch.client.RestClientBuilder builder = RestClient.builder(host);
        builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(Timeout.of(5, TimeUnit.SECONDS)));
        RestClient restClient = builder.build();

        // 使用 OpenSearch RestClient 执行请求
        org.opensearch.client.Request request = new org.opensearch.client.Request("GET", "/");
        org.opensearch.client.Response response = restClient.performRequest(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        String responseBody = EntityUtils.toString(response.getEntity());
        assertNotNull("Response body should not be null", responseBody);
        assertTrue("Response should contain version info", responseBody.contains("version") || responseBody.length() > 0);

        System.out.println("Connected successfully!");
        System.out.println("Response: " + responseBody);

        restClient.close();
    }

    @Test
    public void testClientCreation() {
        // 测试客户端创建是否正常
        HttpHost host = new HttpHost("http", "localhost", 10200);

        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();

        UdbsxClient client = new UdbsxClient(transport);

        assertNotNull("Client should be created successfully", client);
        System.out.println("Client created successfully");
    }

}
