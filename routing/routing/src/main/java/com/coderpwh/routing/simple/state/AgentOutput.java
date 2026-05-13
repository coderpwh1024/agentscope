package com.coderpwh.routing.simple.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentOutput(@JsonProperty("source")String source,@JsonProperty("result") String result) {


    @JsonCreator
    public AgentOutput {}

}
