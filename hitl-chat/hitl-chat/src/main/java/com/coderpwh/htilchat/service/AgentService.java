package com.coderpwh.htilchat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * @author coderpwh
 */
@Service
public class AgentService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    private String apiKey;

    private String modelName;

}
