package net.edwardsonthe.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the TimeOfDay message consumer.
 *
 * <p>Bootstraps the Spring application context. The active Spring profile determines
 * which messaging transport is used. Messages are processed by {@link TimeOfDayConsumer}.
 */
@SpringBootApplication(scanBasePackages = "net.edwardsonthe")
public class ConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerApplication.class, args);
  }
}
