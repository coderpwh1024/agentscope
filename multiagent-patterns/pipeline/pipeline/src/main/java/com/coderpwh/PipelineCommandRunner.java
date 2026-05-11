package com.coderpwh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author coderpwh
 */

@Component
@ConditionalOnProperty(name="pipeline.runner.enabled",havingValue = "true")
public class PipelineCommandRunner implements ApplicationRunner {



     private  static  final Logger log = LoggerFactory.getLogger(PipelineCommandRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
