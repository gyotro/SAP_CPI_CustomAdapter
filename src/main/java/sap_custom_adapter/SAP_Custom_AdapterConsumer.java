package sap_custom_adapter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class SAP_Custom_AdapterConsumer extends ScheduledPollConsumer {

    private static final Logger log = LoggerFactory.getLogger(SAP_Custom_AdapterConsumer.class);

    private final SAP_Custom_AdapterEndpoint endpoint;

    public SAP_Custom_AdapterConsumer(SAP_Custom_AdapterEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        setDelay(endpoint.getPollingInterval());
    }

    @Override
    protected int poll() throws Exception {
        log.info("Polling database with query: " + endpoint.getSelectQuery());

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
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(endpoint.getSelectQuery())) {

            while (rs.next()) {
                Exchange exchange = getEndpoint().createExchange();
                StringBuilder row = new StringBuilder();
                int cols = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    row.append(rs.getString(i));
                    if (i < cols) row.append(",");
                }
                exchange.getIn().setBody(row.toString());
                log.debug("Processing row: " + row);
                getProcessor().process(exchange);
            }
        } catch (Exception e) {
            log.error("Polling error: ", e);
            throw e;
        }

        return 1;
    }
}