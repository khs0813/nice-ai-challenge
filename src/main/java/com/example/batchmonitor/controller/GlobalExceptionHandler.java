package com.example.batchmonitor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, HttpServletRequest request, Model model) {
        log.error("Unexpected error. uri={}", request.getRequestURI(), exception);
        model.addAttribute("path", request.getRequestURI());
        return "error/custom-error";
    }
}
