package com.example.api.sandbox.controller;

import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Flogger
@RestController()
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private Nitrite database;

    @PostMapping(value = "/clear", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> clear() {
        log.atInfo().log("Processing incoming request [POST] /system/clear");
        WriteResult writeResult = database.getRepository(Map.class).remove(ObjectFilters.ALL);
        log.atInfo().log(String.format("Cleared '%s' documents from the database...", writeResult.getAffectedCount()));
        return new ResponseEntity<>(String.format("{\"AffectedCount\":\"%s\"}", writeResult.getAffectedCount()), HttpStatus.OK);
    }

}
