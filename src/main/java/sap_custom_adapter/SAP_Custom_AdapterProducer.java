package sap_custom_adapter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

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
public class SAP_Custom_AdapterProducer extends org.apache.camel.impl.DefaultProducer {

    private final SAP_Custom_AdapterEndpoint endpoint;

    public SAP_Custom_AdapterProducer(SAP_Custom_AdapterEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;

        Long interval = endpoint.getPollingInterval();
        if (interval != null) {
            log.info("Polling interval for producer logic (informative only): {} ms", interval);
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Executing UPDATE query: {}", endpoint.getUpdateQuery());

        String jdbcUrl = String.format("jdbc:sqlserver://%s:%s",
                endpoint.getDbHost(), endpoint.getDbPort());

        if (endpoint.getCustomConnectionString() != null && !endpoint.getCustomConnectionString().isEmpty()) {
            jdbcUrl += ";" + endpoint.getCustomConnectionString();
        }

        log.debug("Constructed JDBC URL: {}", jdbcUrl);

        Properties props = new Properties();
        props.put("user", endpoint.getDbUser());
        props.put("password", endpoint.getDbPassword());

        // Conditionally use Cloud Connector if location ID is provided
        if (endpoint.getCloudConnectorLocation() != null && !endpoint.getCloudConnectorLocation().isEmpty()) {
            log.info("Using Cloud Connector with location ID: {}", endpoint.getCloudConnectorLocation());
            CloudConnectorContext context = new CloudConnectorContext();
            context.setConnectionType(ConnectionType.TCP);

            CloudConnectorProperties ccProperties = ITApiFactory.getService(CloudConnectorProperties.class, context);
            if (ccProperties == null) {
                throw new IllegalStateException("Cloud Connector Properties service not available.");
            }

            props.put("sap.cloud.connector.locationid", endpoint.getCloudConnectorLocation());
        } else {
            log.info("Using direct cloud connection without Cloud Connector");
        }

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
