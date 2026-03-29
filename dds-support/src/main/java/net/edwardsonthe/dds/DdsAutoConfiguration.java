package net.edwardsonthe.dds;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Auto-configuration entry point for the DDS transport module.
 *
 * <p>Enables component scanning of the {@code net.edwardsonthe.dds} package so that
 * DDS adapters and health indicators are discovered automatically by any Spring Boot
 * application that includes {@code dds-support} on its classpath.
 *
 * <p>Only active when the {@code dds} profile is enabled. This eliminates the need
 * for applications to use {@code scanBasePackages} — the transport module is
 * self-configuring.
 */
@Configuration
@Profile("dds")
@ComponentScan(basePackages = "net.edwardsonthe.dds")
public class DdsAutoConfiguration {
}
