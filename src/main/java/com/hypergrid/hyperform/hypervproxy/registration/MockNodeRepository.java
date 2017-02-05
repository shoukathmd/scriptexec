/* COPYRIGHT (C) 2017 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Only required for local end-to-end testing.
 *
 * @author Intesar Mohammed
 */

@Component()
@Qualifier("MockNodeRepository")
public class MockNodeRepository implements NodeRepository {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getHyperCloudUrl() {
        return "http://127.0.0.1:9090/api/1.0/hypercloud-cluster-registration";
    }

    @Override
    public String getHostIp() {
        return "10.10.10.10";
    }

    @Override
    public String getClusterName() {
        return "Mock-Cluster";
    }

    @Override
    public String getNodeName() {
        return "Mock-Node";
    }

    @Override
    public String getCSVPath() {
        return "true";
    }

    @Override
    public String createVMPath() {
        return "";
    }

    @Override
    public String createBSPath() {
        return "";
    }
}
