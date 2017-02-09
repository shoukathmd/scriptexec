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

import java.util.HashMap;
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

    protected String csvPath;
    protected String csvComputeServicePath;
    protected String csvComputeServiceTemplatesPath;
    protected String csvBlockServicePath;

    private final String PROXY_URL = "proxy.url";
    private final String PROXY_USERNAME = "proxy.username";
    private final String PROXY_PASSWORD = "proxy.password";
    private final String CLUSTER_NAME = "cluster.name";
    private final String NODE_NAME = "node.name";
    private final String VM_VHDX_LOC = "vm.vhdx.loc";
    private final String WARNINGS = "warnings";
    private final String CSV_PATH = "vm.template.loc";
    private final String CSV_COMPUTE_SERVICE_PATH = "vm.vhdx.loc";
    private final String CSV_COMPUTE_SERVICE_TEMPLATES_PATH = "vm.template.loc";
    private final String CSV_BLOCK_SERVICE_PATH = "bs.vhdx.loc";


    @Autowired
    public RegistrationUtil(@Value("${server.port}") String port,
                            @Value("${registration.max.retry}") Integer maxRetries,
                            @Value("${mock.service}") Boolean mockService,
                            @Value("${csv.path}") String csvPath,
                            @Value("${csv.compute-service.path}") String csvComputeServicePath,
                            @Value("${csv.templates.path}") String csvComputeServiceTemplatesPath,
                            @Value("${csv.block-service.path}") String csvBlockServicePath,
                            CmdletsService cmdletsService,
                            @Qualifier("NodeRepositoryImpl") NodeRepository nodeRepository,
                            @Qualifier("MockNodeRepository") NodeRepository mockRepository,
                            RegistrationSender registrationSender) {

        this.port = port;
        this.maxRetries = maxRetries;
        this.cmdletsService = cmdletsService;
        this.registrationSender = registrationSender;

        this.csvPath = csvPath;
        this.csvComputeServicePath = csvComputeServicePath;
        this.csvComputeServiceTemplatesPath = csvComputeServiceTemplatesPath;
        this.csvBlockServicePath = csvBlockServicePath;

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

            HyperCloudClusterRegistrationRequest dto = new HyperCloudClusterRegistrationRequest();
            dto.setMap(new HashMap<>());

            String ip = repository.getHostIp();
            if (org.apache.commons.lang3.StringUtils.isEmpty(ip)) {
                sb.append("Can't find host 'IP'.");
            }

            String proxyUrl = "https://" + ip + ":" + port;
            dto.getMap().put(PROXY_URL, proxyUrl);

            String proxyPassword = null;
            Properties properties = ConfigFileUtil.readConfigFile();
            if (properties != null) {
                proxyPassword = properties.getProperty(ConfigFileUtil.PROP_PASS);
            } else {
                sb.append("\n").append("File '" + ConfigFileUtil.CONFIG + "' not found.");
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(proxyPassword)) {
                sb.append("\n")
                        .append("proxy 'password' not found.");
            }
            dto.getMap().put(PROXY_PASSWORD, proxyPassword);

            String clusterName = repository.getClusterName();
            if (org.apache.commons.lang3.StringUtils.isEmpty(clusterName)) {
                sb.append("\n")
                        .append("'cluster' name not found.");
            }
            dto.getMap().put(CLUSTER_NAME, clusterName);

            String nodeName = repository.getNodeName();
            if (org.apache.commons.lang3.StringUtils.isEmpty(nodeName)) {
                sb.append("\n")
                        .append("'node' name not found.");
            }
            dto.getMap().put(NODE_NAME, nodeName);


            String csv = repository.getCSVPath();
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(csv, "true")) {
                dto.getMap().put(CSV_PATH, this.csvPath);
                // create compute service path
                String vmResponse = repository.createVMPath();
                logger.info("Compute-Service path creation response [{}]", vmResponse);
                dto.getMap().put(CSV_COMPUTE_SERVICE_PATH, this.csvComputeServicePath);

                // create templates path
                String templatesResponse = repository.createTemplatesPath();
                logger.info("Compute-Service Templates path creation response [{}]", templatesResponse);
                dto.getMap().put(CSV_COMPUTE_SERVICE_TEMPLATES_PATH, this.csvComputeServiceTemplatesPath);

                //sb.append("\n").append(vmResponse);
                // create block storage service path
                String bsResponse = repository.createBSPath();
                logger.info("Block-Service path creation response [{}]", bsResponse);
                dto.getMap().put(CSV_BLOCK_SERVICE_PATH, this.csvBlockServicePath);
                //sb.append("\n").append(bsResponse);
            } else {
                sb.append("\n")
                        .append("Cluster Shared Volume path 'csv.path' not found.");
            }

            String warnings = sb.toString();
            dto.getMap().put(WARNINGS, warnings);
            logger.info("Sending dto [{}]", dto);

            // TODO - retry on failure
            String response = registrationSender.sendAndReceiveProxy(repository.getHyperCloudUrl(), dto);

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
