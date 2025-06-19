
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

@Slf4j
@Getter
@Setter
public class SAP_Custom_AdapterConsumer extends ScheduledPollConsumer {

    private final SAP_Custom_AdapterEndpoint endpoint;

    public SAP_Custom_AdapterConsumer(SAP_Custom_AdapterEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        log.info("Polling with SELECT query: {}", endpoint.getSelectQuery());

        String connectionString = String.format("jdbc:sqlserver://%s:%s;%s",
                endpoint.getDbHost(),
                endpoint.getDbPort(),
                endpoint.getCustomConnectionString() != null ? endpoint.getCustomConnectionString() : "");

        log.debug("Connecting to database using Cloud Connector at {}", connectionString);

        Properties props = new Properties();
        props.put("user", endpoint.getDbUser());
        props.put("password", endpoint.getDbPassword());
        props.put("sap.cloud.connector.locationid", endpoint.getCloudConnectorLocation());

        try (Connection conn = DriverManager.getConnection(connectionString, props);
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
