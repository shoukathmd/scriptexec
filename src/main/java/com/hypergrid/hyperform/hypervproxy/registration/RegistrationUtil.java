/* COPYRIGHT (C) 2017 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.registration;

import com.hypergrid.hyperform.hypervproxy.config.ConfigFileUtil;
import com.hypergrid.hyperform.hypervproxy.service.CmdletsService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author Intesar Mohammed
 */

@Component
public class RegistrationUtil implements ApplicationListener<ContextRefreshedEvent> {

    final Logger logger = LoggerFactory.getLogger(getClass());

    protected String port;

    protected Integer maxRetries;

    protected CmdletsService cmdletsService;

    protected NodeRepository repository;

    protected RegistrationSender registrationSender;

    @Autowired
    public RegistrationUtil(@Value("${server.port}") String port,
                            @Value("${registration.max.retry}") Integer maxRetries,
                            @Value("${mock.service}") Boolean mockService,
                            CmdletsService cmdletsService,
                            @Qualifier("NodeRepositoryImpl") NodeRepository nodeRepository,
                            @Qualifier("MockNodeRepository") NodeRepository mockRepository,
                            RegistrationSender registrationSender) {

        this.port = port;
        this.maxRetries = maxRetries;
        this.cmdletsService = cmdletsService;
        this.registrationSender = registrationSender;

        repository = nodeRepository;
        if (BooleanUtils.isTrue(mockService)) {
            repository = mockRepository;
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerRetreable();
    }

    /**
     * <p>
     * Read
     * - ip
     * - port
     * - password
     * - username
     * - cluster
     * - node
     * - csv
     * </p>
     * <p>
     * send
     * </p>
     */
    private void registerRetreable() {
        boolean response = false;
        int count = 1;
        while (BooleanUtils.isFalse(response) && count <= this.maxRetries) {
            logger.info("Registration retry #{}", count);
            response = register();
            try {
                if (BooleanUtils.isFalse(response)) {
                    Thread.sleep(600 * 1000);  // sleep for 10 minutes
                } else {
                    logger.info("Registration complete!");
                }
            } catch (InterruptedException e) {
                logger.warn(e.getLocalizedMessage());
            }
        }
    }

    private boolean register() {

        try {
            StringBuilder sb = new StringBuilder();

            String ip = repository.getHostIp();
            if (org.apache.commons.lang3.StringUtils.isEmpty(ip)) {
                sb.append("Can't find host 'IP'.");
            }

            String url = "https://" + ip + ":" + port;

            String password = null;
            Properties properties = ConfigFileUtil.readConfigFile();
            if (properties != null) {
                password = properties.getProperty(ConfigFileUtil.PROP_PASS);
            } else {
                sb.append("\n").append("File '" + ConfigFileUtil.CONFIG + "' not found.");
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(password)) {
                sb.append("\n")
                        .append("proxy 'password' not found.");
            }

            String cluster = repository.getClusterName();
            if (org.apache.commons.lang3.StringUtils.isEmpty(cluster)) {
                sb.append("\n")
                        .append("'cluster' name not found.");
            }

            String node = repository.getNodeName();
            if (org.apache.commons.lang3.StringUtils.isEmpty(node)) {
                sb.append("\n")
                        .append("'node' name not found.");
            }
            String csv = repository.getCSVPath();
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(csv, "true")) {
                // create compute service path
                String vmResponse = repository.createVMPath();
                //sb.append("\n").append(vmResponse);
                // create block storage service path
                String bsResponse = repository.createBSPath();
                //sb.append("\n").append(bsResponse);
            } else {
                sb.append("\n")
                        .append("Cluster Shared Volume path 'csv.path' not found.");
            }

            String warnings = sb.toString();
            logger.info("Sending url [{}] password [{}] cluster [{}] node [{}] csv [{}] warnings [{}]", url, password, cluster, node, csv, warnings);

            // TODO - retry on failure
            String response = registrationSender.sendAndReceiveProxy(repository.getHyperCloudUrl(), url, password, cluster, node, warnings);

            if (StringUtils.equals(response, "Error")) {
                return false;
            }
            logger.info("Registration response [{}]", response);
            return true;
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            return false;
        }

    }

}
