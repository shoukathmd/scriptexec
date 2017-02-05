package com.hypergrid.hyperform.hypervproxy.registration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Intesar Mohammed
 */
public class HyperCloudClusterRegistrationRequest implements Serializable {

    protected Map<String, String> map = new HashMap<>();

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}

