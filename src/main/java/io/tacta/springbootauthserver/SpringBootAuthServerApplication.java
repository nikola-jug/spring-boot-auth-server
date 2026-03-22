package io.tacta.springbootauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBootAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAuthServerApplication.class, args);
    }

}
