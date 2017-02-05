/* COPYRIGHT (C) 2017 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.registration;

import com.hypergrid.hyperform.hypervproxy.service.CmdletsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Intesar Mohammed
 */

@Component()
@Qualifier("NodeRepositoryImpl")
public class NodeRepositoryImpl implements NodeRepository {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected CmdletsService cmdletsService;

    @Value("${csv.path}")
    public String CSV;// = "C:\\ClusterStorage\\Volume1";

    @Value("${csv.compute-service.path}")
    public String CSV_VM_PATH;// = "C:\\ClusterStorage\\Volume1\\HyperCloud\\Compute-Service\\";

    @Value("${csv.block-service.path}")
    public String CSV_BS_PATH;// = "C:\\ClusterStorage\\Volume1\\HyperCloud\\Block-Service\\";


    @Override
    public String getHyperCloudUrl() {
        String endpoint = "https://hypercloud.local/api/1.0/hypercloud-cluster-registration";
        return endpoint;
    }

    @Override
    public String getHostIp() {
        try {
            String ip = InetAddress.getLocalHost().getHostName();
            return ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getClusterName() {
        try {
            String cmdlet = "Get-Cluster | Select-Object Name";
            String response = cmdletsService.executeCommand("", cmdlet, "", "", "", null);
            logger.info("cmdlet [{}] response [{}]", cmdlet, response);
            Set<String> names = extractNames(response);
            if (!CollectionUtils.isEmpty(names)) {
                return names.iterator().next();
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getNodeName() {
        try {
            String cmdlet = "Get-VMHost | Select-Object Name";
            String response = cmdletsService.executeCommand("", cmdlet, "", "", "", null);
            logger.info("cmdlet [{}] response [{}]", cmdlet, response);
            Set<String> names = extractNames(response);
            if (!CollectionUtils.isEmpty(names)) {
                return names.iterator().next();
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getCSVPath() {
        try {
            String cmdlet = "Test-Path " + CSV;
            String response = cmdletsService.executeCommand("", cmdlet, "", "", "", null);
            logger.info("cmdlet [{}] response [{}]", cmdlet, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String createVMPath() {
        return createPath(CSV_VM_PATH);
    }

    @Override
    public String createBSPath() {
        return createPath(CSV_BS_PATH);
    }

    public String createPath(String path) {
        try {
            String cmdlet = "New-Item " + path + " -type directory";
            String response = cmdletsService.executeCommand("", cmdlet, "", "", "", null);
            logger.info("cmdlet [{}] response [{}]", cmdlet, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> extractNames(String rawText) {
        Set<String> names = new HashSet<>();

        try {
            Pattern regex = Pattern.compile("^[a-zA-Z0-9_ -\\\\]*",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

            Matcher regexMatcher = regex.matcher(StringUtils.trimWhitespace(rawText));


            // ignore 1st & 2nd lines?
            int ignoreCount = 2;
            while (regexMatcher.find()) {
                if (--ignoreCount < 0) {
                    //logger.info("[" + regexMatcher.group(1) + "]");
                    String name = regexMatcher.group(0);
                    if (!StringUtils.isEmpty(name)) {
                        names.add(name.trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return names;
    }
}
