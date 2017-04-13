/* COPYRIGHT (C) 2017 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.config;

import java.io.*;
import java.util.Properties;

/**
 * @author Intesar Mohammed
 */
public class ConfigFileUtil {

    //public static final String CONFIG = "C://opt//dchq//application.properties";
    //public static final String CONFIG = "C://ClusterStorage//HyperCloud_Reserved//proxy//application.properties";

    private String CONFIG;
    private static ConfigFileUtil instance;

    private ConfigFileUtil(String configLocation) {
        CONFIG = configLocation;
    }

    public static ConfigFileUtil getInstance(String configLocation) {
        if (instance == null) {
            instance = new ConfigFileUtil(configLocation);
        }
        return instance;
    }

    public static final String PROP_PASS = "hypervproxy.password";

    public void writeConfigFile(String password) {

        Properties prop = null;
        OutputStream output = null;

        try {

            output = new FileOutputStream(CONFIG);

            // set the properties value
            prop = readConfigFile();

            if (prop == null) {
                System.out.println("Cannot read file: " + CONFIG);
            }
            prop.setProperty(PROP_PASS, password);

            System.out.println("Writing password to the file: " + CONFIG);
            prop.store(output, null);

        } catch (Exception io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public Properties readConfigFile() {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(CONFIG);

            // load a properties file
            prop.load(input);

            return prop;

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
