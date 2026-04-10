/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.client.opensearch.integTest.aws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opensearch.client.opensearch.UdbsxClient;
import org.opensearch.client.opensearch._types.UdbsxException;
import org.opensearch.client.opensearch.cluster.GetClusterSettingsRequest;

public class AwsSdk2SecurityIT extends AwsSdk2TransportTestCase {
    private static final String DEFAULT_MESSAGE = "authentication/authorization failure";

    @Test
    public void testUnAuthorizedException() {
        final UdbsxClient client = getClient(false, null, null);
        final GetClusterSettingsRequest request = new GetClusterSettingsRequest.Builder().includeDefaults(true).build();
        final UdbsxException ex = assertThrows(UdbsxException.class, () -> client.cluster().getSettings(request));
        assertFalse(ex.getMessage().contains(DEFAULT_MESSAGE));
    }
}
