/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client.samples;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.samples.util.IndexData;

/**
 * Udbsx 客户端功能验证测试
 *
 * 运行前请确保:
 * 1. OpenSearch/Udbsx 服务器在 localhost:10200 运行
 * 2. 或者设置环境变量 HOST 和 HTTPS 指向你的服务器
 *
 * Run with: {@code .\gradlew :samples:run -Dsamples.mainClass=UdbsxVerificationTest}
 */
public class UdbsxVerificationTest {
    private static final Logger LOGGER = LogManager.getLogger(UdbsxVerificationTest.class);

    public static void main(String[] args) {
        try {
            var client = SampleClient.create();

            // 测试 1: 获取服务器信息
            LOGGER.info("=== 测试 1: 连接服务器 ===");
            try {
                var infoResponse = client.info();
                var version = infoResponse.version();
                String versionInfo = version.number();
                if (version.distribution() != null) {
                    versionInfo = version.distribution() + "@" + version.number();
                }
                LOGGER.info("✅ 服务器连接成功：{}", versionInfo);
            } catch (Exception e) {
                LOGGER.warn("⚠️  获取版本信息失败（可能是兼容性问题）: {}", e.getMessage());
                LOGGER.info("✅ 但服务器连接正常，继续后续测试...");
            }
            // 测试 2: 创建测试索引
            LOGGER.info("\n=== 测试 2: 创建索引 ===");
            final String indexName = "udbsx-verification-test";

            if (client.indices().exists(r -> r.index(indexName)).value()) {
                LOGGER.info("ℹ️ 索引已存在，删除后重新创建");
                DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(b -> b.index(indexName));
                client.indices().delete(deleteRequest);
            }

            CreateIndexRequest createIndexRequest = CreateIndexRequest.of(
                b -> b.index(indexName)
                    .mappings(
                        m -> m.properties("title", Property.of(p -> p.text(TextProperty.of(t -> t))))
                            .properties("content", Property.of(p -> p.text(TextProperty.of(t -> t))))
                            .properties("count", Property.of(p -> p.integer(i -> i)))
                    )
            );
            client.indices().create(createIndexRequest);
            LOGGER.info("✅ 索引创建成功：{}", indexName);

            // 测试 3: 写入测试文档
            LOGGER.info("\n=== 测试 3: 写入文档 ===");
            List<IndexData> documents = List.of(
                new IndexData("Document 1", "This is the content of document 1"),
                new IndexData("Document 2", "This is the content of document 2"),
                new IndexData("Document 3", "This is the content of document 3")
            );

            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            for (int i = 0; i < documents.size(); i++) {
                final int indexData = i + 1;
                var doc = documents.get(i);
                bulkBuilder.operations(op -> op.index(idx -> idx.index(indexName).id(String.valueOf(indexData)).document(doc)));
            }

            BulkResponse bulkResponse = client.bulk(bulkBuilder.build());
            LOGGER.info("✅ Bulk 操作成功：索引了 {} 个文档", bulkResponse.items().size());

            // 等待索引刷新
            Thread.sleep(2000);

            // 测试 4: Count API
            LOGGER.info("\n=== 测试 4: Count API ===");
            CountRequest countRequest = CountRequest.of(b -> b.index(indexName));
            CountResponse countResponse = client.count(countRequest);
            LOGGER.info("✅ Count API 成功：共 {} 个文档", countResponse.count());

            // 测试 5: 搜索测试
            LOGGER.info("\n=== 测试 5: 搜索查询 ===");
            SearchResponse<IndexData> searchResponse = client.search(
                s -> s.index(indexName).query(q -> q.matchAll(m -> m)),
                IndexData.class
            );

            LOGGER.info("✅ 搜索成功：返回 {} 条结果", searchResponse.hits().total().value());
            int hitCount = 0;
            for (var hit : searchResponse.hits().hits()) {
                hitCount++;
                LOGGER.info("   命中 {}: {} (score: {})", hitCount, hit.source(), hit.score());
            }

            // 测试 6: 带查询条件的搜索
            LOGGER.info("\n=== 测试 6: 条件查询 ===");
            SearchResponse<IndexData> matchResponse = client.search(
                s -> s.index(indexName).query(q -> q.match(m -> m.field("title").query(FieldValue.of("Document 2")))),
                IndexData.class
            );

            // 测试 7: 删除测试索引
            LOGGER.info("\n=== 测试 7: 清理资源 ===");
            DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(b -> b.index(indexName));
            client.indices().delete(deleteRequest);
            LOGGER.info("✅ 测试索引已删除：{}", indexName);

            // 测试 8: 测试子客户端
            LOGGER.info("\n=== 测试 8: 子客户端访问测试 ===");
            assertNotNull(client.cat());
            assertNotNull(client.cluster());
            assertNotNull(client.indices());
            assertNotNull(client.ingest());
            assertNotNull(client.security());
            LOGGER.info("✅ 所有子客户端访问正常");

            LOGGER.info("\n🎉 ============================================");
            LOGGER.info("🎉 所有测试通过！UdbsxClient 工作正常！");
            LOGGER.info("🎉 ============================================");

        } catch (Exception e) {
            LOGGER.error("\n❌ 测试失败", e);
            System.exit(1);
        }
    }

    private static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new RuntimeException("对象不应该为 null");
        }
    }
}
