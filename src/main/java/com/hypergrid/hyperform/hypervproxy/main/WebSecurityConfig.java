package com.hypergrid.hyperform.hypervproxy.main;


import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${hypervproxy.password}")
    protected String password;

    @Value("${hypervproxy.username}")
    protected String username;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .anyRequest().hasAnyRole("USER")
                .and().httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        if (StringUtils.isEmpty(password)) {
            this.password = RandomStringUtils.randomAlphanumeric(12);
        }

        logger.info("*****************************************************************");
        logger.info("API Credentials: username [{}] password [{}]", username, password);
        logger.info("*****************************************************************");

        auth
                .inMemoryAuthentication()
                .withUser(username).password(this.password).roles("USER");
    }
}