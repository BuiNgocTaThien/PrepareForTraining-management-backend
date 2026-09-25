package com.fpt.preparefortraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PrepareForTrainingApplication {
  public static void main(String[] args) {
    SpringApplication.run(PrepareForTrainingApplication.class, args);
  }
}
