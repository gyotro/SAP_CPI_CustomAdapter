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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.*;

/**
 * Represents a www.Sample.com Camel endpoint.
 */
@Getter
@Setter
@ToString
@UriEndpoint(firstVersion = "1.0.0", scheme = "jdbcCustom", title = "JDBC Custom Polling Adapter", syntax = "jdbcCustom://name", producerOnly = false, consumerOnly = false)
public class SAP_Custom_AdapterEndpoint extends DefaultEndpoint {

    @UriParam(label = "common", description = "JDBC URL to connect to the DB")
    private String jdbcUrl;

    @UriParam(label = "security", description = "DB username")
    private String username;

    @UriParam(label = "security", secret = true, description = "DB password")
    private String password;

    @UriParam(label = "consumer", description = "SQL query to execute periodically")
    private String selectQuery;

    @UriParam(label = "producer", description = "SQL update to mark data as processed")
    private String updateQuery;

    @UriParam(label = "consumer", defaultValue = "60000", description = "Polling interval in milliseconds")
    private long pollingInterval = 60000L;

    public SAP_Custom_AdapterEndpoint(String uri, String remaining, SAP_Custom_AdapterComponent component) {
        super(uri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new SAP_Custom_AdapterProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new SAP_Custom_AdapterConsumer(this, processor);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
