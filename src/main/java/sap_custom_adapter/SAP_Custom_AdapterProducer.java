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
package sap_custom_adapter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ccs.adapter.CloudConnectorContext;
import com.sap.it.api.ccs.adapter.CloudConnectorProperties;
import com.sap.it.api.ccs.adapter.ConnectionType;

@Slf4j
@Getter
@Setter
public class SAP_Custom_AdapterProducer extends DefaultProducer {

    private final SAP_Custom_AdapterEndpoint endpoint;

    public SAP_Custom_AdapterProducer(SAP_Custom_AdapterEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;

    }

    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Executing UPDATE query: {}", endpoint.getUpdateQuery());

        CloudConnectorContext context = new CloudConnectorContext();
        context.setConnectionType(ConnectionType.TCP);

        CloudConnectorProperties ccProperties = ITApiFactory.getService(CloudConnectorProperties.class, context);
        if (ccProperties == null) {
            throw new IllegalStateException("Cloud Connector Properties service not available.");
        }

        String jdbcUrl = String.format("jdbc:sqlserver://%s:%s",
                endpoint.getDbHost(), endpoint.getDbPort());

        if (endpoint.getCustomConnectionString() != null && !endpoint.getCustomConnectionString().isEmpty()) {
            jdbcUrl += ";" + endpoint.getCustomConnectionString();
        }

        log.debug("Constructed JDBC URL: {}", jdbcUrl);

        Properties props = new Properties();
        props.put("user", endpoint.getDbUser());
        props.put("password", endpoint.getDbPassword());
        props.put("sap.cloud.connector.locationid", endpoint.getCloudConnectorLocation());

        try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
             PreparedStatement stmt = conn.prepareStatement(endpoint.getUpdateQuery())) {

            int rowsAffected = stmt.executeUpdate();
            log.info("Update completed. Rows affected: {}", rowsAffected);

        } catch (Exception e) {
            log.error("Error during update execution", e);
            throw e;
        }
    }
}
