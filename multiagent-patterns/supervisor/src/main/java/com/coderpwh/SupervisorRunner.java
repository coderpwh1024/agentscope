package com.coderpwh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * SupervisorRunner
 *
 * @author coderpwh
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "supervisor.run-examples", havingValue = "true")
public class SupervisorRunner implements ApplicationRunner {


    private  final  static Logger log = LoggerFactory.getLogger(SupervisorRunner.class);


    @Override
    public void run(ApplicationArguments args) throws Exception {

    }

}
