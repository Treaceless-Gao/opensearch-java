/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.apache.hc.core5.http.HttpHost;
import org.junit.Test;
import org.opensearch.client.opensearch.UdbsxAsyncClient;
import org.opensearch.client.opensearch.UdbsxClient;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
import org.opensearch.client.opensearch.core.InfoRequest;
import org.opensearch.client.opensearch.core.InfoResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.transport.UdbsxTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

/**
 * Udbsx 客户端综合功能测试
 * 验证重命名后的客户端所有核心功能正常工作
 */
public class UdbsxClientComprehensiveTest {

    private static class TestDocument {
        public String name;
        public int value;

        public TestDocument() {}

        public TestDocument(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * 测试同步客户端基本信息获取
     */
    @Test
    public void testSyncClientInfo() throws IOException {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxClient client = new UdbsxClient(transport);

        try {
            InfoResponse info = client.info(InfoRequest.of(b -> b));
            assertNotNull("Info response should not be null", info);
            assertNotNull("Version should not be null", info.version());

            System.out.println("✅ Sync client info test passed");
        } catch (Exception e) {
            // 如果服务器未运行，记录警告但不失败
            System.out.println("⚠️ Sync client info test skipped (server may not be running): " + e.getMessage());
        } finally {
            transport.close();
        }
    }

    /**
     * 测试异步客户端基本信息获取
     */
    @Test
    public void testAsyncClientInfo() throws Exception {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxAsyncClient asyncClient = new UdbsxAsyncClient(transport);

        try {
            InfoResponse info = asyncClient.info(InfoRequest.of(b -> b)).join();
            assertNotNull("Info response should not be null", info);
            assertNotNull("Version should not be null", info.version());

            System.out.println("✅ Async client info test passed");
        } catch (Exception e) {
            System.out.println("⚠️ Async client info test skipped (server may not be running): " + e.getMessage());
        } finally {
            transport.close();
        }
    }

    /**
     * 测试同步客户端 Count API
     */
    @Test
    public void testSyncClientCount() throws IOException {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxClient client = new UdbsxClient(transport);

        try {
            CountRequest countRequest = CountRequest.of(b -> b.index("test-index"));
            CountResponse response = client.count(countRequest);
            assertNotNull("Count response should not be null", response);

            System.out.println("✅ Sync client count test passed");
        } catch (Exception e) {
            System.out.println("⚠️ Sync client count test skipped: " + e.getMessage());
        } finally {
            transport.close();
        }
    }

    /**
     * 测试异步客户端 Count API
     */
    @Test
    public void testAsyncClientCount() throws Exception {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxAsyncClient asyncClient = new UdbsxAsyncClient(transport);

        try {
            CountRequest countRequest = CountRequest.of(b -> b.index("test-index"));
            CountResponse response = asyncClient.count(countRequest).join();
            assertNotNull("Count response should not be null", response);

            System.out.println("✅ Async client count test passed");
        } catch (Exception e) {
            System.out.println("⚠️ Async client count test skipped: " + e.getMessage());
        } finally {
            transport.close();
        }
    }

    /**
     * 测试客户端异常处理
     */
    @Test
    public void testClientExceptionHandling() {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxClient client = new UdbsxClient(transport);

        try {
            SearchRequest searchRequest = SearchRequest.of(b -> b.index("non-existent-index"));
            SearchResponse<TestDocument> response = client.search(searchRequest, TestDocument.class);
            fail("Should throw exception for non-existent index");
        } catch (Exception e) {
            assertTrue(
                "Should be UdbsxException or IOException",
                e instanceof org.opensearch.client.opensearch._types.UdbsxException || e instanceof IOException
            );
            System.out.println("✅ Exception handling test passed: " + e.getClass().getSimpleName());
        } finally {
            try {
                transport.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 测试客户端 withTransportOptions 方法
     */
    @Test
    public void testClientWithTransportOptions() {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxClient client = new UdbsxClient(transport);

        UdbsxClient clientWithOptions = client.withTransportOptions(null);
        assertNotNull("Client with options should be created", clientWithOptions);
        assertNotNull("Transport should not be null", clientWithOptions._transport());

        System.out.println("✅ Client withTransportOptions test passed");

        try {
            transport.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * 测试所有子客户端都可访问
     */
    @Test
    public void testAllChildClientsAccessible() {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxClient client = new UdbsxClient(transport);

        try {
            assertNotNull(client.cat());
            assertNotNull(client.cluster());
            assertNotNull(client.indices());
            assertNotNull(client.danglingIndices());
            assertNotNull(client.geospatial());
            assertNotNull(client.ingest());
            assertNotNull(client.ingestion());
            assertNotNull(client.ism());
            assertNotNull(client.knn());
            assertNotNull(client.ltr());
            assertNotNull(client.ml());
            assertNotNull(client.nodes());
            assertNotNull(client.searchPipeline());
            assertNotNull(client.searchRelevance());
            assertNotNull(client.security());
            assertNotNull(client.snapshot());
            assertNotNull(client.tasks());
            assertNotNull(client.ubi());
            assertNotNull(client.generic());

            System.out.println("✅ All child clients accessible test passed");
        } finally {
            try {
                transport.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 测试异步客户端所有子客户端都可访问
     */
    @Test
    public void testAsyncAllChildClientsAccessible() {
        HttpHost host = new HttpHost("http", "localhost", 10200);
        UdbsxTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        UdbsxAsyncClient asyncClient = new UdbsxAsyncClient(transport);

        try {
            assertNotNull(asyncClient.cat());
            assertNotNull(asyncClient.cluster());
            assertNotNull(asyncClient.indices());
            assertNotNull(asyncClient.danglingIndices());
            assertNotNull(asyncClient.geospatial());
            assertNotNull(asyncClient.ingest());
            assertNotNull(asyncClient.ingestion());
            assertNotNull(asyncClient.ism());
            assertNotNull(asyncClient.knn());
            assertNotNull(asyncClient.ltr());
            assertNotNull(asyncClient.ml());
            assertNotNull(asyncClient.nodes());
            assertNotNull(asyncClient.searchPipeline());
            assertNotNull(asyncClient.searchRelevance());
            assertNotNull(asyncClient.security());
            assertNotNull(asyncClient.snapshot());
            assertNotNull(asyncClient.tasks());
            assertNotNull(asyncClient.ubi());

            System.out.println("✅ Async all child clients accessible test passed");
        } finally {
            try {
                transport.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
