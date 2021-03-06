/* COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved. */
package com.hypergrid.hyperform.hypervproxy.controller;

import com.hypergrid.hyperform.hypervproxy.service.CmdletsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Cmdlet Controller
 *
 * @author Intesar Mohammed
 */

@RestController
@RequestMapping(value = "/HyperVProxy/api/1.0/Cmdlets")
public class CmdletsController {

    final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    protected CmdletsService cmdletsService;

    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<String> run(@RequestParam(value = "interpreter", defaultValue = "") String interpreter,
                               @RequestParam(value = "cmdlet", defaultValue = "") String cmdlet,
                               @RequestParam(value = "parameters", defaultValue = "") String parameters,
                               @RequestParam(value = "ext", defaultValue = "") String ext,
                               @RequestParam(value = "timeout", defaultValue = "") String timeout) {


        logger.info("Cmdlet [{}] received...", cmdlet);
        logger.debug("Run... interpreter [{}] cmdlet [{}] parameters [{}] ext [{}] timeout [{}] ", interpreter, cmdlet, parameters, ext, timeout);
        String response = cmdletsService.executeCommand(interpreter, cmdlet, parameters, ext, timeout, null);

        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

}