package sap_custom_adapter;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAP_Custom_AdapterEndpoint extends DefaultEndpoint {

    private static final Logger log = LoggerFactory.getLogger(SAP_Custom_AdapterEndpoint.class);

    private String cloudConnectorLocation;
    private String dbHost;
    private String dbPort;
    private String customConnectionString;
    private String dbUser;
    private String dbPassword;
    private String selectQuery;
    private String updateQuery;
    private long pollingInterval;

    public SAP_Custom_AdapterEndpoint(String endpointUri, String remaining, SAP_Custom_AdapterComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        log.info("Creating producer for JDBC update query: " + updateQuery);
        return new SAP_Custom_AdapterProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        log.info("Creating consumer for JDBC polling query: " + selectQuery);
        return new SAP_Custom_AdapterConsumer(this, processor);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getCloudConnectorLocation() {
        return cloudConnectorLocation;
    }

    public void setCloudConnectorLocation(String cloudConnectorLocation) {
        this.cloudConnectorLocation = cloudConnectorLocation;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getCustomConnectionString() {
        return customConnectionString;
    }

    public void setCustomConnectionString(String customConnectionString) {
        this.customConnectionString = customConnectionString;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public String getUpdateQuery() {
        return updateQuery;
    }

    public void setUpdateQuery(String updateQuery) {
        this.updateQuery = updateQuery;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public void logEndpointConfiguration() {
        String configLog = "Endpoint configuration: host=" + dbHost +
                ", port=" + dbPort +
                ", user=" + dbUser +
                ", cloudConnectorLocation=" + cloudConnectorLocation +
                ", customString=" + customConnectionString +
                ", pollingInterval=" + pollingInterval;
        log.debug(configLog);
    }
}
