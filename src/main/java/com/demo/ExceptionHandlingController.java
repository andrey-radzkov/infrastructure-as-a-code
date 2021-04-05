package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionHandlingController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingController.class);

    @ExceptionHandler(Exception.class)
    public String handleError(HttpServletRequest req, Exception ex) {
        log.error("Request: " + req.getRequestURL() + " raised " + ex);
        return ex.getMessage();
    }
}
