package net.edwardsonthe.dds;

import net.edwardsonthe.common.TimeOfDayEvent;
import net.edwardsonthe.messages.TimeOfDayMessage;
import net.edwardsonthe.messages.TimeOfDayMessageDataWriter;
import com.rti.dds.infrastructure.InstanceHandle_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Outbound channel adapter that bridges Spring Integration to RTI Connext DDS.
 *
 * <p>Consumes {@link TimeOfDayEvent} messages from the {@code timeOfDayOutboundChannel}
 * and publishes them as DDS {@link TimeOfDayMessage} instances via the DataWriter.
 *
 * <p>Only active when the {@code dds} profile is enabled and {@code dds.role=producer}.
 */
@Component
@Profile("dds")
@ConditionalOnProperty(name = "dds.role", havingValue = "producer")
public class DdsOutboundChannelAdapter {

  private static final Logger log = LoggerFactory.getLogger(DdsOutboundChannelAdapter.class);

  private final TimeOfDayMessageDataWriter writer;

  public DdsOutboundChannelAdapter(TimeOfDayMessageDataWriter writer) {
    this.writer = writer;
  }

  /**
   * Receives a {@link TimeOfDayEvent} from the outbound channel, converts it to
   * the DDS {@link TimeOfDayMessage}, and writes it to the DDS topic.
   *
   * @param message the Spring Integration message containing the event payload
   */
  @ServiceActivator(inputChannel = "timeOfDayOutboundChannel")
  public void handleMessage(Message<TimeOfDayEvent> message) {
    TimeOfDayEvent event = message.getPayload();

    TimeOfDayMessage ddsMessage = new TimeOfDayMessage();
    ddsMessage.timestamp = event.getTimestamp();
    ddsMessage.messageId = event.getMessageId();
    ddsMessage.quote = event.getQuote();

    writer.write(ddsMessage, InstanceHandle_t.HANDLE_NIL);
    log.debug("Sent to DDS: [{}]", event.getMessageId());
  }
}
