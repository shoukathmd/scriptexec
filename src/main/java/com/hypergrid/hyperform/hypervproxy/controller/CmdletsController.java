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

        if (StringUtils.isEmpty(interpreter)) {
            interpreter = defaultInterpreter;
        }

        if (StringUtils.isEmpty(cmdlet)) {
            cmdlet = defaultCmdlet;
        }

        if (StringUtils.isEmpty(ext)) {
            ext = defaultScriptExt;
        }

        if (StringUtils.isEmpty(timeout)) {
            timeout = defaultTimeout;
        }

        try {
            cmdlet = java.net.URLDecoder.decode(cmdlet, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        logger.info("Cmdlet [{}] received...", cmdlet);
        logger.debug("Run... interpreter [{}] cmdlet [{}] parameters [{}] ext [{}] timeout [{}] ", interpreter, cmdlet, parameters, ext, timeout);
        String response = cmdletsService.executeCommand(interpreter, cmdlet, parameters, ext, Long.parseLong(timeout), null);

        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/list-images", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity<List<String>> listImages(@RequestParam(value = "directory", defaultValue = "") String directory,
                                            @RequestParam(value = "extension", defaultValue = "") String extension) {

        if (StringUtils.isEmpty(directory)) {
            directory = templatesDir;
        }

        if (StringUtils.isEmpty(extension)) {
            extension = templatesExt;
        }

        logger.info("List-Images directory [{}] extension [{}] ", directory, extension);

        List<String> response = cmdletsService.listFiles(directory, extension);

        return new ResponseEntity<List<String>>(response, HttpStatus.OK);
    }

}