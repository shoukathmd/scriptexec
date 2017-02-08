/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.service;

/**
 * Cmdlet Service - wrapper for running powershell.
 *
 * @author Intesar Mohammed
 */
public interface CmdletsService {

    /**
     * Executes powershell cmdlet
     *
     * @param interpreter         - e.g. powershell
     * @param command             - powershell cmdlet
     * @param args                - optional args
     * @param ext                 - e.g. ps1
     * @param timeout             - e.g. 20 minutes (120000)
     * @param inputValidExitCodes - exit codes to determine execution was successful or failure
     * @return cmdlet response
     */
    public String executeCommand(String interpreter, String command, String args, String ext, String timeout, int[] inputValidExitCodes);


    /**
     * Executes powershell cmdlet
     *
     * @param command - powershell cmdlet
     * @return cmdlet response
     */
    public String executeCommand(String command);
}
