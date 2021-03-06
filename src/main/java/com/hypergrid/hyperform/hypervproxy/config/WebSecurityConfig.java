/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.config;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author Intesar Mohammed
 */

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${hypervproxy.password}")
    protected String password;

    @Value("${hypervproxy.username}")
    protected String username;

    @Value("${hypervproxy.password.generate}")
    protected String generatePassword;

    @Value("${config.location}")
    protected String reservedConfigLocation;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .anyRequest().hasAnyRole("USER")
                .and().httpBasic();
    }

    protected @Value("${info.build.version}")
    String version;

    @PostConstruct
    public void printVersion() {
        logger.info("############### Proxy Version: [{}] #################", version);
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        logger.info("*****************************************************************");
        if (StringUtils.isEmpty(password)
                || org.apache.commons.lang3.StringUtils.equalsIgnoreCase("yes", generatePassword)
                || org.apache.commons.lang3.StringUtils.equalsIgnoreCase("true", generatePassword)) {

            logger.info("Generating new password on start...");
            this.password = RandomStringUtils.randomAlphanumeric(64);
            logger.info("Generated new password [{}]", this.password);

            logger.info("Persisting password hash [{}]", this.password);
            //ConfigFileUtil.getInstance(reservedConfigLocation).writeConfigFile(this.password);
        }

        this.password = DigestUtils.sha256Hex(this.password);

        logger.info("API Credentials: username [{}] password [{}]", this.username, this.password);
        logger.info("*****************************************************************");

        auth
                .inMemoryAuthentication()
                .withUser(this.username).password(this.password).roles("USER");
    }
}