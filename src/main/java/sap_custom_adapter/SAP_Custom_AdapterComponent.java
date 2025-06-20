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

import java.util.Map;
import java.util.logging.Logger;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages.
 */

public class SAP_Custom_AdapterComponent extends UriEndpointComponent {

    public static final Logger log = Logger.getLogger(SAP_Custom_AdapterComponent.class.getName());

    public SAP_Custom_AdapterComponent() {
        super(SAP_Custom_AdapterEndpoint.class);
    }

    public SAP_Custom_AdapterComponent(CamelContext context) {
        super(context, SAP_Custom_AdapterEndpoint.class);
    }
    //private Logger LOG = LoggerFactory.getLogger(SAP_Custom_AdapterComponent.class);

//    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
//        LOG.info("Creating the end point");
//        final Endpoint endpoint = new SAP_Custom_AdapterEndpoint(uri, remaining, this);
//        setProperties(endpoint, parameters);
//        return endpoint;
//    }
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        log.info("Creating endpoint for URI: " + uri);
        SAP_Custom_AdapterEndpoint endpoint = new SAP_Custom_AdapterEndpoint(uri, remaining, this);
        setProperties(endpoint, parameters); // sets @UriParam fields
        return endpoint;
    }
}
