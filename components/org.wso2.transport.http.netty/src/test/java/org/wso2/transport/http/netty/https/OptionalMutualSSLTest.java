/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.https;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contentaware.listeners.EchoMessageListener;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.util.TestUtil;

import java.util.HashMap;

import static org.wso2.transport.http.netty.contract.Constants.HTTPS_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.OPTIONAL;

/**
 * Tests for mutual ssl setting verifyClient to optional.
 */
public class OptionalMutualSSLTest {

    private static final Logger LOG = LoggerFactory.getLogger(OptionalMutualSSLTest.class);

    private static HttpClientConnector httpClientConnector;
    private HttpWsConnectorFactory factory;
    private ServerConnector connector;

    @BeforeClass
    public void setup() throws Exception {
        factory = new DefaultHttpWsConnectorFactory();

        ListenerConfiguration listenerConfiguration = getListenerConfiguration();

        connector = factory
                .createServerConnector(TestUtil.getDefaultServerBootstrapConfig(), listenerConfiguration);
        ServerConnectorFuture future = connector.start();
        future.setHttpConnectorListener(new EchoMessageListener());
        future.sync();

        httpClientConnector = factory.createHttpsClientConnector(new HashMap<>(), getSenderConfigs());
    }

    private ListenerConfiguration getListenerConfiguration() {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(TestUtil.SERVER_PORT3);
        listenerConfiguration.setVerifyClient(OPTIONAL);
        listenerConfiguration.setTLSStoreType("JKS");
        listenerConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        listenerConfiguration.setKeyStoreFile(TestUtil.getAbsolutePath(TestUtil.KEY_STORE_FILE_PATH));
        listenerConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        listenerConfiguration.setKeyStorePass(TestUtil.KEY_STORE_PASSWORD);
        listenerConfiguration.setScheme(HTTPS_SCHEME);
        return listenerConfiguration;
    }

    private SenderConfiguration getSenderConfigs() {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setTrustStoreFile(TestUtil.getAbsolutePath(TestUtil.TRUST_STORE_FILE_PATH));
        senderConfiguration.setTrustStorePass(TestUtil.KEY_STORE_PASSWORD);
        senderConfiguration.setTLSStoreType("JKS");
        senderConfiguration.setScheme(HTTPS_SCHEME);
        return senderConfiguration;
    }

    @Test
    public void testMutualSslOptional() {
        TestUtil.testHttpsPost(httpClientConnector, TestUtil.SERVER_PORT3);
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        connector.stop();
        httpClientConnector.close();
        try {
            factory.shutdown();
        } catch (Exception e) {
            LOG.warn("Interrupted while waiting for response", e);
        }
    }
}
