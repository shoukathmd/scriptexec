package com.hypergrid.hyperform.hypervproxy.registration;

/**
 * @author Intesar Mohammed
 */
public interface NodeRepository {

    String getHyperCloudUrl();

    String getHostIp();

    String getClusterName();

    String getNodeName();

    String getCSVPath();

    String createVMPath();

    String createBSPath();
}
