/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client.opensearch.integTest;

import java.io.IOException;
import java.util.Optional;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.UdbsxClient;
import org.opensearch.client.transport.UdbsxTransport;
import org.opensearch.common.settings.Settings;

public interface OpenSearchTransportSupport {
    default boolean isHttps() {
        return Optional.ofNullable(System.getProperty("https")).map("true"::equalsIgnoreCase).orElse(false);
    }

    default UdbsxClient buildJavaClient(Settings settings, HttpHost[] hosts) throws IOException {
        return new UdbsxClient(buildTransport(settings, hosts));
    }

    UdbsxTransport buildTransport(Settings settings, HttpHost[] hosts) throws IOException;
}
