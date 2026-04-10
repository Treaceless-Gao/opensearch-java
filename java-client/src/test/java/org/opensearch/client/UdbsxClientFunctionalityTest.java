/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client;

import static org.junit.Assert.assertNotNull;

import org.apache.hc.core5.http.HttpHost;
import org.junit.Test;
import org.opensearch.client.opensearch.UdbsxAsyncClient;
import org.opensearch.client.opensearch.UdbsxClient;
import org.opensearch.client.transport.UdbsxTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

/**
 * 验证 Udbsx 客户端功能测试
 */
public class UdbsxClientFunctionalityTest {

    @Test
    public void testSyncClientCreation() {
        HttpHost host = new HttpHost("http", "localhost", 10200);

        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();

        UdbsxClient client = new UdbsxClient(transport);

        assertNotNull("Sync client should be created", client);
        assertNotNull("Transport should not be null", client._transport());

        // 测试子客户端
        assertNotNull(client.cat());
        assertNotNull(client.cluster());
        assertNotNull(client.indices());
        assertNotNull(client.searchPipeline());

        System.out.println("✅ Sync client functionality verified");
    }

    @Test
    public void testAsyncClientCreation() {
        HttpHost host = new HttpHost("http", "localhost", 10200);

        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();

        UdbsxAsyncClient asyncClient = new UdbsxAsyncClient(transport);

        assertNotNull("Async client should be created", asyncClient);
        assertNotNull("Transport should not be null", asyncClient._transport());

        // 测试子客户端
        assertNotNull(asyncClient.cat());
        assertNotNull(asyncClient.cluster());
        assertNotNull(asyncClient.indices());

        System.out.println("✅ Async client functionality verified");
    }

    @Test
    public void testClientWithTransportOptions() {
        HttpHost host = new HttpHost("http", "localhost", 10200);

        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();

        UdbsxClient client = new UdbsxClient(transport);

        // 测试 withTransportOptions
        UdbsxClient clientWithOptions = client.withTransportOptions(null);
        assertNotNull("Client with options should be created", clientWithOptions);

        System.out.println("✅ Client with transport options verified");
    }
}
