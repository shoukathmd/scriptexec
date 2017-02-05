/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Rejects any request with unsupport CMD.
 *
 * @author Intesar Mohammed
 * @see CmdletsService
 */
@Service
public class CmdFilter {

    final Logger logger = LoggerFactory.getLogger(getClass());


    @Value("${cmdlet.default}")
    protected String defaultCmdlet;


    public String filter(String command, String args) {
        // TODO check for 'Chain Commands' - ; & && ||
        // TODO check for white listed patterns

        /*
                    directory creation - safe
                    "New-Item " + vhdInstanceBase + " -type directory -force";
                    copy vhd
                    "cp " + image + " " + vhd;
                    VM creation
                    "New-VM -Name " + instanceName + " -MemoryStartupBytes " + memory + " -Generation " + generation + " -VHDPath " + vhd + " -Path " + vhdBase + " -SwitchName \"" + network + "\""; " -ComputerName " + host;
                    VM disk resize
                    "Resize-VHD -Path " + vhd + " -SizeBytes " + disk;
                    VM CPU resize
                    "Set-VMProcessor " + instanceName + " -Count " + cpu;                   c
                    VM Mem resize
                    cmdlet = "Set-VMMemory " + instanceName + " -DynamicMemoryEnabled true";  c
                    VM Network set
                    "Set-VMNetworkAdapterVlan -VMName " + instanceName + " -Access -VlanId " + vlanId; c
                    VM start
                    "Start-VM -Name " + instanceName; c
                    VM add to cluster
                    "Add-ClusterVirtualMachineRole -VMName " + instanceName + "  -Cluster " + clusterName;
                    VM get name
                    "Get-VM -Name  " + instanceName; " c | Select -ExpandProperty NetworkAdapters | Select IPAddresses";
                    VM stop
                    "Stop-VM -Name " + id + " -Force";
                    VM destroy
                    "Remove-VM -Name " + id + " -Force";
                    Directory delete
                    "Remove-Item " + vhdBase + " -recurse";
                    VM start
                    "Start-VM -Name " + id; c
                    VM stop
                    "Stop-VM -Name " + id + " -Force"; c
                    VM restart
                    "Restart-VM -Name " + id + " -Force"; c
                    Cluster get name
                    "Get-ClusterNode | Select-Object Name"
                    Node get name
                    "Get-VMHost | Select-Object Name"
                    List files
                    " Get-ChildItem " + templateLoc + " | Select-Object FullName";
                    List files
                    " Invoke-Command -ComputerName " + computerName + " -ScriptBlock { Get-ChildItem " + templateLoc + " | Select-Object FullName }";

                    "Get-VMSwitch"; "  | Select-Object Name";
         */

        return null;
    }


}
