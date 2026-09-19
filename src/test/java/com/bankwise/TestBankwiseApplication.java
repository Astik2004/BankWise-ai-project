package com.bankwise;

import org.springframework.boot.SpringApplication;

public class TestBankwiseApplication {

    public static void main(String[] args) {
        SpringApplication.from(BankwiseApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
