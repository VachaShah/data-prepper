/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  The OpenSearch Contributors require contributions made to
 *  this file be licensed under the Apache-2.0 license or a
 *  compatible open source license.
 *
 *  Modifications Copyright OpenSearch Contributors. See
 *  GitHub history for details.
 */

package com.amazon.dataprepper.plugins.prepper.peerforwarder;

import com.amazon.dataprepper.model.configuration.PluginSetting;
import com.amazon.dataprepper.plugins.prepper.peerforwarder.discovery.DiscoveryMode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PeerForwarderConfigTest {
    private static final String PIPELINE_NAME = "pipelineName";
    private static final String VALID_SSL_KEY_CERT_FILE = PeerForwarderConfigTest.class.getClassLoader()
            .getResource("test-crt.crt").getFile();
    private static final String EMPTY_SSL_KEY_CERT_FILE = "";
    private static final String INVALID_SSL_KEY_CERT_FILE = "path/to/file";
    private static List<String> TEST_ENDPOINTS = Arrays.asList("172.0.0.1", "172.0.0.2");

    private MockedStatic<PeerClientPool> mockedPeerClientPoolClass;

    @Mock
    private PeerClientPool peerClientPool;

    @Before
    public void setUp() {
        mockedPeerClientPoolClass = mockStatic(PeerClientPool.class);
        mockedPeerClientPoolClass.when(PeerClientPool::getInstance).thenReturn(peerClientPool);
        doNothing().when(peerClientPool).setSsl(anyBoolean());
        doNothing().when(peerClientPool).setSslKeyCertChainFile(any(File.class));
    }

    @After
    public void tearDown() {
        // Need to release static mock as otherwise it will remain active on the thread when running other tests
        mockedPeerClientPoolClass.close();
    }

    @Test
    public void testBuildConfigNoSSL() {
        final int testNumSpansPerRequest = 2;
        final int testTimeout = 300;
        final HashMap<String, Object> settings = new HashMap<>();
        settings.put(PeerForwarderConfig.DISCOVERY_MODE, DiscoveryMode.STATIC.toString());
        settings.put(PeerForwarderConfig.STATIC_ENDPOINTS, TEST_ENDPOINTS);
        settings.put(PeerForwarderConfig.MAX_NUM_SPANS_PER_REQUEST, testNumSpansPerRequest);
        settings.put(PeerForwarderConfig.TIME_OUT, testTimeout);
        settings.put(PeerForwarderConfig.SSL, false);

        final PeerForwarderConfig peerForwarderConfig = PeerForwarderConfig.buildConfig(
                new PluginSetting("peer_forwarder", settings){{ setPipelineName(PIPELINE_NAME); }});

        Assert.assertEquals(testNumSpansPerRequest, peerForwarderConfig.getMaxNumSpansPerRequest());
        Assert.assertEquals(testTimeout, peerForwarderConfig.getTimeOut());
    }

    @Test
    public void testBuildConfigInvalidSSL() {
        final HashMap<String, Object> settings = new HashMap<>();
        settings.put(PeerForwarderConfig.DISCOVERY_MODE, DiscoveryMode.STATIC.toString());
        settings.put(PeerForwarderConfig.STATIC_ENDPOINTS, TEST_ENDPOINTS);
        settings.put(PeerForwarderConfig.SSL, true);

        settings.put(PeerForwarderConfig.SSL_KEY_CERT_FILE, EMPTY_SSL_KEY_CERT_FILE);
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            PeerForwarderConfig.buildConfig(new PluginSetting("peer_forwarder", settings){{ setPipelineName(PIPELINE_NAME); }});
        });

        settings.put(PeerForwarderConfig.SSL_KEY_CERT_FILE, null);
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            PeerForwarderConfig.buildConfig(new PluginSetting("peer_forwarder", settings){{ setPipelineName(PIPELINE_NAME); }});
        });

        settings.put(PeerForwarderConfig.SSL_KEY_CERT_FILE, INVALID_SSL_KEY_CERT_FILE);
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            PeerForwarderConfig.buildConfig(new PluginSetting("peer_forwarder", settings){{ setPipelineName(PIPELINE_NAME); }});
        });
    }

    @Test
    public void testBuildConfigValidSSL() {
        final HashMap<String, Object> settings = new HashMap<>();
        settings.put(PeerForwarderConfig.DISCOVERY_MODE, DiscoveryMode.STATIC.toString());
        settings.put(PeerForwarderConfig.STATIC_ENDPOINTS, TEST_ENDPOINTS);
        settings.put(PeerForwarderConfig.SSL, true);
        settings.put(PeerForwarderConfig.SSL_KEY_CERT_FILE, VALID_SSL_KEY_CERT_FILE);

        PeerForwarderConfig.buildConfig(new PluginSetting("peer_forwarder", settings){{ setPipelineName(PIPELINE_NAME); }});
        verify(peerClientPool, times(1)).setSsl(true);
        verify(peerClientPool, times(1)).setSslKeyCertChainFile(new File(VALID_SSL_KEY_CERT_FILE));
    }
}