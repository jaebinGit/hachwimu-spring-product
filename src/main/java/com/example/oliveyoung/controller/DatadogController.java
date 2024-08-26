package com.example.oliveyoung.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatadogController {

    private static final Logger logger = LoggerFactory.getLogger(DatadogController.class);

    @GetMapping("/products/api/data")
    public String getData(@RequestHeader(value = "x-datadog-trace-id", required = false) String traceId) {
        if (traceId != null) {
            logger.info("Datadog Trace ID: " + traceId);
        }
        return "Data fetched successfully";
    }
}