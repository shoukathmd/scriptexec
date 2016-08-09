/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.service;


import org.apache.commons.exec.*;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    @Override
    public String executeCommand(String interpreter, String command, String args, String ext, long timeout, int[] inputValidExitCodes) {
        String scriptFile = null;
        String response = null;
        try {
            String fileName = "hypervproxy-" + UUID.randomUUID().toString() + "." + ext;
            scriptFile = /*dchqDir + File.separator +*/ fileName;

            FileUtils.writeStringToFile(new File(scriptFile), command, Charsets.UTF_8);
            logger.debug("Created script file [{}]", fileName);

            String execCmd = interpreter + " " + scriptFile + " " + args;

            response = executeCommand(execCmd, timeout, inputValidExitCodes);
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
