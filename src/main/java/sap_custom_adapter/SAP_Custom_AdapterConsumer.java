package sap_custom_adapter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.apache.camel.impl.ScheduledPollConsumer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.sap.it.api.ITApiFactory;
import com.sap.it.api.ccs.adapter.CloudConnectorContext;
import com.sap.it.api.ccs.adapter.CloudConnectorProperties;
import com.sap.it.api.ccs.adapter.ConnectionType;

@Slf4j
@Getter
@Setter
public class SAP_Custom_AdapterConsumer extends ScheduledPollConsumer {

    private final SAP_Custom_AdapterEndpoint endpoint;

    public SAP_Custom_AdapterConsumer(SAP_Custom_AdapterEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        Long interval = endpoint.getPollingInterval();
        if (interval != null) {
            setDelay(interval);
            setInitialDelay(interval);
            log.info("Polling interval configured to {} ms (applied to delay and initial delay)", interval);
        } else {
            log.warn("Polling interval is not set. Using default values.");
        }

        setUseFixedDelay(true); // Important to ensure fixed delay behavior
    }

    @Override
    protected int poll() throws Exception {
        log.info("Polling with SELECT query: {}", endpoint.getSelectQuery());

        CloudConnectorContext context = new CloudConnectorContext();
        context.setConnectionType(ConnectionType.TCP);

        CloudConnectorProperties ccProperties = ITApiFactory.getService(CloudConnectorProperties.class, context);
        if (ccProperties == null) {
            throw new IllegalStateException("Cloud Connector Properties service not available.");
        }

        // Manually construct JDBC URL using host and port
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
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(endpoint.getSelectQuery())) {

            while (rs.next()) {
                Exchange exchange = getEndpoint().createExchange();
                StringBuilder row = new StringBuilder();
                int cols = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    row.append(rs.getString(i)).append(i < cols ? "," : "");
                }
                exchange.getIn().setBody(row.toString());
                getProcessor().process(exchange);
            }

        } catch (Exception e) {
            log.error("Error during polling", e);
            throw e;
        }

        return 1;
    }
}
