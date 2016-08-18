/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.service;


import org.apache.commons.exec.*;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Cmdlet Service implementation - wrapper for running powershell.
 *
 * @author Intesar Mohammed
 * @see CmdletsService
 */
@Service
public class CmdletsServiceImpl implements CmdletsService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${templates.dir}")
    protected String templatesDir;

    @Value("${templates.ext}")
    protected String templatesExt;


    @Value("${cmdlet.max.timeout}")
    protected String defaultTimeout;


    @Value("${cmdlet.interpreter}")
    protected String defaultInterpreter;


    @Value("${cmdlet.script.ext}")
    protected String defaultScriptExt;


    @Value("${cmdlet.default}")
    protected String defaultCmdlet;

    @Value("${vhdx.destination}")
    protected String vhdxDestination;

    protected static final String VHDX_BASE_LOC_PATTERN = "$VHDX_BASE_LOC";


    @Override
    public List<String> listFiles(String directory, String extension) {

        List<String> response = new ArrayList<String>();
        String[] extensions = null;
        try {
            if (org.springframework.util.StringUtils.isEmpty(directory)) {
                directory = templatesDir;
            }

            if (org.springframework.util.StringUtils.isEmpty(extension)) {
                extension = templatesExt;
            }

            if (!org.springframework.util.StringUtils.isEmpty(extension)) {
                extensions = org.springframework.util.StringUtils.split(extension, ",");
            }

            Collection<File> files = FileUtils.listFiles(new File(directory), extensions, false);

            for (File file : files) {
                logger.info("Name [{}} absolute-path [{}] canonical-path [{}] parent [{}] path [{}]", file.getName(), file.getAbsolutePath(), file.getParent(), file.getPath());
                response.add(file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
            response.add(e.getLocalizedMessage());
        }
        return response;
    }


    @Override
    public String executeCommand(String interpreter, String command, String args, String ext, String timeout, int[] inputValidExitCodes) {
        String scriptFile = null;
        String response = null;
        Long timeoutMilli = null;
        try {

            if (org.springframework.util.StringUtils.isEmpty(interpreter)) {
                interpreter = defaultInterpreter;
            }

            if (org.springframework.util.StringUtils.isEmpty(command)) {
                command = defaultCmdlet;
            }

            if (StringUtils.contains(command, VHDX_BASE_LOC_PATTERN)) {
                command = StringUtils.replace(command, VHDX_BASE_LOC_PATTERN, vhdxDestination);
            }

            if (org.springframework.util.StringUtils.isEmpty(ext)) {
                ext = defaultScriptExt;
            }

            if (org.springframework.util.StringUtils.isEmpty(timeout)) {
                timeout = defaultTimeout;
            }
            timeoutMilli = Long.parseLong(timeout);


            try {
                command = java.net.URLDecoder.decode(command, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }

            String fileName = "hypervproxy-" + UUID.randomUUID().toString() + "." + ext;
            scriptFile = /*dchqDir + File.separator +*/ fileName;
            logger.info("Executing cmdlet [{}]", command);
            FileUtils.writeStringToFile(new File(scriptFile), command, Charsets.UTF_8);
            logger.debug("Created script file [{}]", fileName);

            String execCmd = interpreter + " -file " + scriptFile + " " + args;

            response = executeCommand(execCmd, timeoutMilli, inputValidExitCodes);
            logger.debug("Cmdlet response [{}]", response);

        } catch (Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
            String errorMsg = ex.getLocalizedMessage();
            return errorMsg;
        } finally {
            if (StringUtils.isNotEmpty(scriptFile)) {
                FileUtils.deleteQuietly(FileUtils.getFile(scriptFile));
            }
        }

        return response;
    }

    private String executeCommand(String command, long timeout, int[] exitCodes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //Process p;
        try {
            logger.debug("Executing Cmdlet [{}]", command);

            CommandLine cmdLine = CommandLine.parse(command);
            Executor executor = new DefaultExecutor();
            if (exitCodes != null && exitCodes.length > 0) {
                logger.debug("Setting user input exit codes [{}]", exitCodes);
                executor.setExitValues(exitCodes);
            }

            // Set timeout
            ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
            executor.setWatchdog(watchdog);


            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

            executor.setStreamHandler(streamHandler);
            executor.execute(cmdLine);

        } catch (ExecuteException e) {
            logger.warn(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return outputStream.toString();
    }


}
