package com.coderpwh.advanced.hits;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author coderpwh
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class HitlInteractionExample {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();



}
