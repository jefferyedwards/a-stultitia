package net.edwardsonthe.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the TimeOfDay message producer.
 *
 * <p>Bootstraps the Spring application context. The active Spring profile determines
 * which messaging transport is used. The publish loop runs via {@link TimeOfDayProducer}.
 */
@SpringBootApplication(scanBasePackages = "net.edwardsonthe")
public class ProducerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProducerApplication.class, args);
  }
}
