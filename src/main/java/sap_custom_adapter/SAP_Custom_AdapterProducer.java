package sap_custom_adapter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ccs.adapter.CloudConnectorContext;
import com.sap.it.api.ccs.adapter.CloudConnectorProperties;
import com.sap.it.api.ccs.adapter.ConnectionType;

class SAP_Custom_AdapterProducer extends DefaultProducer {

    private static final Logger log = LoggerFactory.getLogger(SAP_Custom_AdapterProducer.class);

    private final SAP_Custom_AdapterEndpoint endpoint;

    public SAP_Custom_AdapterProducer(SAP_Custom_AdapterEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Processing exchange in Producer with update query: " + endpoint.getUpdateQuery());

        String connectionString = String.format("jdbc:sqlserver://%s:%s;%s",
                endpoint.getDbHost(),
                endpoint.getDbPort(),
                endpoint.getCustomConnectionString() != null ? endpoint.getCustomConnectionString() : "");

        Properties props = new Properties();
        props.put("user", endpoint.getDbUser());
        props.put("password", endpoint.getDbPassword());
        if (endpoint.getCloudConnectorLocation() != null && !endpoint.getCloudConnectorLocation().isEmpty()) {
            props.put("sap.cloud.connector.locationid", endpoint.getCloudConnectorLocation());
            log.info("Using Cloud Connector with location: " + endpoint.getCloudConnectorLocation());
        } else {
            log.info("Connecting without Cloud Connector.");
        }

        try (Connection conn = DriverManager.getConnection(connectionString, props);
             Statement stmt = conn.createStatement()) {

            int result = stmt.executeUpdate(endpoint.getUpdateQuery());
            log.debug("Update result: " + result);

            exchange.getMessage().setBody("Updated rows: " + result);
        } catch (Exception e) {
            log.error("Error during update execution: ", e);
            throw e;
        }
    }
}
