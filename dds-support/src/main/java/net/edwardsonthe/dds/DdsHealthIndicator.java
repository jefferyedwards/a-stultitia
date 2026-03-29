package net.edwardsonthe.dds;

import com.rti.dds.domain.DomainParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that reports the status of the DDS {@link DomainParticipant}.
 *
 * <p>Appears as {@code "dds"} in the {@code /actuator/health} response (Spring Boot
 * strips the {@code HealthIndicator} suffix automatically). Reports {@code UP} with
 * the domain ID and application role when the participant is available, or {@code DOWN}
 * if the participant is null.
 */
@Component
public class DdsHealthIndicator implements HealthIndicator {

  private final DomainParticipant participant;
  private final String role;

  /**
   * @param participant the DDS DomainParticipant bean
   * @param appName     the Spring application name, used as the role detail
   */
  public DdsHealthIndicator(DomainParticipant participant,
                            @Value("${spring.application.name:unknown}") String appName) {
    this.participant = participant;
    this.role = appName;
  }

  @Override
  public Health health() {
    if (participant != null) {
      return Health.up()
          .withDetail("domainId", participant.get_domain_id())
          .withDetail("role", role)
          .build();
    }
    return Health.down()
        .withDetail("error", "DomainParticipant is null")
        .build();
  }
}
