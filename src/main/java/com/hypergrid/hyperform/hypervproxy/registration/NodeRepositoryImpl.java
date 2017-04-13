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

import com.kstruct.gethostname4j.Hostname;

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
    protected String CSV;

    @Value("${csv.compute-service.path1}")
    protected String CSV_VM_PATH1;

    @Value("${csv.compute-service.path2}")
    protected String CSV_VM_PATH2;

    @Value("${csv.block-service.path}")
    protected String CSV_BS_PATH;

    @Value("${csv.templates.path}")
    protected String CSV_TEMPLATES_PATH;

    @Value("${registration.url}")
    protected String registrationUrl;


    @Override
    public String getHyperCloudUrl() {
        return registrationUrl;
    }

    @Override
    public String getHostIp() {
        try {
            String ip = Hostname.getHostname();
            //String ip = InetAddress.getLocalHost().getHostName();
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
            String response = cmdletsService.executeCommand(cmdlet);
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
            String response = cmdletsService.executeCommand(cmdlet);
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
            String response = cmdletsService.executeCommand(cmdlet);
            logger.info("cmdlet [{}] response [{}]", cmdlet, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String createVMPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(createPath(CSV_VM_PATH1));
        sb.append("\n");
        sb.append(createPath(CSV_VM_PATH2));
        return sb.toString();
    }

    @Override
    public String createTemplatesPath() {
        return createPath(CSV_TEMPLATES_PATH);
    }

    @Override
    public String createBSPath() {
        return createPath(CSV_BS_PATH);
    }

    private String createPath(String path) {
        try {
            String testPath = "Test-Path " + path;
            String isPathPresent = cmdletsService.executeCommand(testPath);

            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase("true", isPathPresent)) {
                logger.info("Path [{}] exists", path);
                return "true";
            }

            String cmdlet = "New-Item " + path + " -type directory";
            String response = cmdletsService.executeCommand("", cmdlet, "", "", "", null);
            logger.info("cmdlet [{}] response [{}]", cmdlet, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<String> extractNames(String rawText) {
        Set<String> names = new HashSet<>();

        try {

            if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(rawText, "Exception")) {
                logger.info("Ignoring response [{}] because of 'Exception'", rawText);
                return names;
            }

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
