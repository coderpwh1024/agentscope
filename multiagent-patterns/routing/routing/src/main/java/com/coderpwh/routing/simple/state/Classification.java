package com.coderpwh.routing.simple.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Classification(@JsonProperty("source") String source, @JsonProperty("query") String query) {


    @JsonCreator
    public Classification {
    }

}
