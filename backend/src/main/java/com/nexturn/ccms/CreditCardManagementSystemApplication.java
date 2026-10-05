package com.nexturn.ccms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CreditCardManagementSystemApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(CreditCardManagementSystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CreditCardManagementSystemApplication.class, args);
        logger.info("Credit card management system application started successfully.");
    }
}