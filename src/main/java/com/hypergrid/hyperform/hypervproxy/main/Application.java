/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * App starting point.
 *
 * @author Intesar Mohammed
 */
@SpringBootApplication(scanBasePackages = {"com.hypergrid.hyperform.hypervproxy"})
public class Application {
    final Logger logger = LoggerFactory.getLogger(getClass());

    public Application() {
        logger.info("HyperVProxy...initialized!");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}