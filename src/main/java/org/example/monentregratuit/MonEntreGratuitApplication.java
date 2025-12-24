package org.example.monentregratuit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonEntreGratuitApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonEntreGratuitApplication.class, args);
    }

}
