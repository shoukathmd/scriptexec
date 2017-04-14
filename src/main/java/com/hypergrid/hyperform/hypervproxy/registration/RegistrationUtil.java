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
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Properties;

/**
 * @author Intesar Mohammed
 */

@Component
//@PropertySource(value = {"${config.location}"})
public class RegistrationUtil implements ApplicationListener<ContextRefreshedEvent> {

    final Logger logger = LoggerFactory.getLogger(getClass());

    protected String configLocation;
    protected String proxyLeaderIp;
    protected String port;

    protected Integer maxRetries;

    protected CmdletsService cmdletsService;

    protected NodeRepository repository;

    protected RegistrationSender registrationSender;


    protected String csvPath1;
    //protected String csvPath2;
    protected String csvComputeServiceTemplatesPath;

    protected String csvComputeServicePath1;
    //protected String csvComputeServicePath2;


    protected String csvBlockServicePath;

    protected String hyperVueEndpoint;
    protected String hyperVueUsername;
    protected String hyperVuePassword;

    protected ConfigFileUtil configFileUtil;

    private final String PROXY_URL = "proxy.url";
    private final String PROXY_USERNAME = "proxy.username";
    private final String PROXY_PASSWORD = "proxy.password";
    private final String CLUSTER_NAME = "cluster.name";
    private final String NODE_NAME = "node.name";
    private final String VM_VHDX_LOC = "vm.vhdx.loc";
    private final String WARNINGS = "warnings";


    // template loc
    private final String CSV_COMPUTE_SERVICE_TEMPLATES_PATH = "vm.template.loc";

    // compute csv
    private final String CSV_COMPUTE_SERVICE_PATH1 = "vm.vhdx.loc1";
    private final String CSV_COMPUTE_SERVICE_PATH2 = "vm.vhdx.loc2";

    // block csv
    private final String CSV_BLOCK_SERVICE_PATH = "bs.vhdx.loc";

    // hypervue
    private final String HYPERVUE_ENDPOINT = "hv.endpoint";
    private final String HYPERVUE_USERNAME = "hv.username";
    private final String HYPERVUE_PASSWORD = "hv.password";


    @Autowired
    public RegistrationUtil(@Value("${server.port}") String port,
                            @Value("${registration.max.retry}") Integer maxRetries,
                            @Value("${mock.service}") Boolean mockService,
                            @Value("${csv.path1}") String csvPath1,
                            //@Value("${csv.path2}") String csvPath2,
                            @Value("${csv.compute-service.path1}") String csvComputeServicePath1,
                            //@Value("${csv.compute-service.path2}") String csvComputeServicePath2,
                            @Value("${csv.templates.path}") String csvComputeServiceTemplatesPath,
                            @Value("${csv.block-service.path}") String csvBlockServicePath,
                            @Value("${proxy.leader.ip}") String proxyLeaderIp,
                            @Value("${config.location}") String configLocation,

                            @Value("${hcim.endpoint}") String hyperVueEndpoint,
                            @Value("${hcim.username}") String hyperVueUsername,
                            @Value("${hcim.password}") String hyperVuePassword,

                            CmdletsService cmdletsService,
                            @Qualifier("NodeRepositoryImpl") NodeRepository nodeRepository,
                            @Qualifier("MockNodeRepository") NodeRepository mockRepository,
                            RegistrationSender registrationSender) {


        this.configLocation = configLocation;
        configFileUtil = ConfigFileUtil.getInstance(this.configLocation);

        this.proxyLeaderIp = proxyLeaderIp;
        this.port = port;
        this.maxRetries = maxRetries;

        this.cmdletsService = cmdletsService;
        this.registrationSender = registrationSender;

        this.csvPath1 = csvPath1;
        //this.csvPath2 = csvPath2;
        this.csvComputeServiceTemplatesPath = csvComputeServiceTemplatesPath;

        this.csvComputeServicePath1 = csvComputeServicePath1;
        //this.csvComputeServicePath2 = csvComputeServicePath2;

        this.csvBlockServicePath = csvBlockServicePath;

        this.hyperVueEndpoint = hyperVueEndpoint;
        this.hyperVueUsername = hyperVueUsername;
        this.hyperVuePassword = hyperVuePassword;

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
                    //Thread.sleep(600 * 1000);  // sleep for 10 minutes
                } else {
                    logger.info("Registration complete!");
                }
            } catch (Exception e) {
                logger.warn(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Register1:
     * Compute-Service1
     * Compute-Service2
     * Block-Service1
     * Block-Service2
     * HyperVue
     *
     * @return
     */
    private boolean register() {

        try {
            StringBuilder sb = new StringBuilder();

            HyperCloudClusterRegistrationRequest dto = new HyperCloudClusterRegistrationRequest();
            dto.setMap(new HashMap<>());

            // proxy leader
            String ip = proxyLeaderIp;
            if (StringUtils.isEmpty(ip)) {
                ip = repository.getHostIp();
            }
            if (StringUtils.isEmpty(ip)) {
                sb.append("Can't find host 'IP'.");
            }

            String proxyUrl = "https://" + ip + ":" + port;
            dto.getMap().put(PROXY_URL, proxyUrl);

            // proxy password
            String proxyPassword = null;
            Properties properties = configFileUtil.readConfigFile();
            if (properties != null) {
                proxyPassword = properties.getProperty(ConfigFileUtil.PROP_PASS);
            } else {
                sb.append("\n").append("Config file '" + configLocation + "' not found.");
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(proxyPassword)) {
                sb.append("\n")
                        .append("proxy 'password' not found.");
            }
            dto.getMap().put(PROXY_PASSWORD, proxyPassword);

            // cluster
            String clusterName = repository.getClusterName();
            if (org.apache.commons.lang3.StringUtils.isEmpty(clusterName)) {
                sb.append("\n")
                        .append("'cluster' name not found.");
            }
            dto.getMap().put(CLUSTER_NAME, clusterName);

            // node
            String nodeName = repository.getNodeName();
            if (org.apache.commons.lang3.StringUtils.isEmpty(nodeName)) {
                sb.append("\n")
                        .append("'node' name not found.");
            }
            dto.getMap().put(NODE_NAME, nodeName);


            // csv paths
            String csv = repository.getCSVPath();
            //if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(csv, "true")) {
                //dto.getMap().put(CSV_PATH, this.csvPath);
                // create compute service path
                String vmResponse = repository.createVMPath();

                logger.info("Compute-Service path creation response [{}]", vmResponse);
                dto.getMap().put(CSV_COMPUTE_SERVICE_PATH1, this.csvComputeServicePath1);
                //dto.getMap().put(CSV_COMPUTE_SERVICE_PATH2, this.csvComputeServicePath2);

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
            //} else {
            //    sb.append("\n")
            //            .append("Cluster Shared Volume path 'csv.path' not found.");
            //}

            // hypervue
            dto.getMap().put(HYPERVUE_ENDPOINT, hyperVueEndpoint);
            dto.getMap().put(HYPERVUE_USERNAME, hyperVueUsername);
            dto.getMap().put(HYPERVUE_PASSWORD, hyperVuePassword);


            String warnings = sb.toString();
            dto.getMap().put(WARNINGS, warnings);
            logger.info("Sending dto [{}]", dto);

            // TODO - retry on failure
            String response = registrationSender.sendAndReceiveProxy(repository.getHyperCloudUrl(), dto);

            if (StringUtils.equals(response, "Error")) {
                return true;
            }
            logger.info("Registration response [{}]", response);
            return true;
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            logger.warn("Registration failed....");
            return true;
        }

    }

}
