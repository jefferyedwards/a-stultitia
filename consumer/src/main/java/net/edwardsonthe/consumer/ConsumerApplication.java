package net.edwardsonthe.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the TimeOfDay message consumer.
 *
 * <p>Bootstraps the Spring application context, which initializes DDS entities
 * via {@link DdsConfig} and registers the {@link TimeOfDayConsumer} listener.
 * The application stays alive as long as the Spring context is open.
 */
@SpringBootApplication(scanBasePackages = "net.edwardsonthe")
public class ConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerApplication.class, args);
  }
}
