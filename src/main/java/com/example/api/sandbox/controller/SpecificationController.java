package com.example.api.sandbox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.sandbox.service.SpecificationService;

import lombok.extern.flogger.Flogger;

@RestController
@Flogger
public class SpecificationController {

    @Autowired
    private SpecificationService specificationsService;

    @GetMapping(value = "/specification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleSpecificationRequest() throws Throwable {
        log.atInfo().log("Getting the specification");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-yaml");
        return new ResponseEntity<>(specificationsService.getApiSpecification().getRaw(), httpHeaders, HttpStatus.OK);
    }

}
