package net.edwardsonthe.consumer;

import net.edwardsonthe.common.TimeOfDayEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Processes incoming {@link TimeOfDayEvent} messages from the inbound channel.
 *
 * <p>This class has no dependency on any messaging transport (DDS, Kafka, etc.). It
 * receives {@link TimeOfDayEvent} objects via a Spring Integration
 * {@link ServiceActivator @ServiceActivator} on the {@code timeOfDayInboundChannel}.
 * The active Spring profile determines which inbound adapter feeds messages into
 * that channel.
 */
@Component
public class TimeOfDayConsumer {

  private static final Logger log = LoggerFactory.getLogger(TimeOfDayConsumer.class);

  private final Counter receivedCounter;

  public TimeOfDayConsumer(MeterRegistry meterRegistry) {
    this.receivedCounter = Counter.builder("messages.received")
        .description("Total messages received")
        .register(meterRegistry);
  }

  /**
   * Processes a single {@link TimeOfDayEvent} received from the inbound channel.
   *
   * @param message the Spring Integration message containing the event payload
   */
  @ServiceActivator(inputChannel = "timeOfDayInboundChannel")
  public void handleMessage(Message<TimeOfDayEvent> message) {
    TimeOfDayEvent event = message.getPayload();
    receivedCounter.increment();
    log.info("Received [{}]: {} — {}", event.getMessageId(), event.getTimestamp(), event.getQuote());
  }
}
