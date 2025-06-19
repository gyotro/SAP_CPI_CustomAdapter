package sap_custom_adapter;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URISyntaxException;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
//import org.apache.camel.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.*;

/**
 * Represents a www.Sample.com Camel endpoint.
 */
@Getter
@Setter
@ToString
@Slf4j
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "jdbcCustom",
        title = "JDBC Custom Polling Adapter",
        syntax = "jdbcCustom://name",
        producerOnly = false,
        consumerOnly = false
 //       category = {Category.DATABASE}
)
public class SAP_Custom_AdapterEndpoint extends DefaultEndpoint {

    @UriParam(label = "connection", description = "Cloud Connector Location")
    private String cloudConnectorLocation;

    @UriParam(label = "connection", description = "Database Host")
    private String dbHost;

    @UriParam(label = "connection", description = "Database Port")
    private String dbPort;

    @UriParam(label = "connection", description = "Custom JDBC connection string suffix")
    private String customConnectionString;

    @UriParam(label = "security", description = "Database User")
    private String dbUser;

    @UriParam(label = "security", description = "Database Password", secret = true)
    private String dbPassword;

    @UriParam(label = "query", description = "SQL SELECT query for polling")
    private String selectQuery;

    @UriParam(label = "query", description = "SQL UPDATE query for outbound data")
    private String updateQuery;

    @UriParam(label = "polling", description = "Polling interval in milliseconds", defaultValue = "10000")
    private long pollingInterval = 600000L;



    public SAP_Custom_AdapterEndpoint(String uri, String remaining, SAP_Custom_AdapterComponent component) {
        super(uri, component);
        log.info("Endpoint created with URI: {}", uri);
    }

    @Override
    public Producer createProducer() throws Exception {
        log.info("Creating producer for JDBC update query: {}", updateQuery);
        return new SAP_Custom_AdapterProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        log.info("Creating consumer for JDBC polling query: {}", selectQuery);
        return new SAP_Custom_AdapterConsumer(this, processor);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
