package net.edwardsonthe.dds;

import net.edwardsonthe.common.TimeOfDayEvent;
import net.edwardsonthe.messages.TimeOfDayMessage;
import net.edwardsonthe.messages.TimeOfDayMessageDataReader;
import net.edwardsonthe.messages.TimeOfDayMessageSeq;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * Inbound channel adapter that bridges RTI Connext DDS to Spring Integration.
 *
 * <p>Extends {@link DataReaderAdapter} to receive DDS callbacks. Each incoming
 * {@link TimeOfDayMessage} is converted to a transport-neutral {@link TimeOfDayEvent}
 * and sent into the {@code timeOfDayInboundChannel}.
 *
 * <p>Only active when the {@code dds} profile is enabled and {@code dds.role=consumer}.
 */
@Component
@Profile("dds")
@ConditionalOnProperty(name = "dds.role", havingValue = "consumer")
public class DdsInboundChannelAdapter extends DataReaderAdapter {

  private static final Logger log = LoggerFactory.getLogger(DdsInboundChannelAdapter.class);

  private final MessageChannel inboundChannel;

  public DdsInboundChannelAdapter(
      @Qualifier("timeOfDayInboundChannel") MessageChannel inboundChannel) {
    this.inboundChannel = inboundChannel;
  }

  /**
   * Called by DDS when one or more samples are available. Converts each valid
   * sample to a {@link TimeOfDayEvent} and sends it to the inbound channel.
   *
   * @param reader the DDS DataReader that has data available
   */
  @Override
  public void on_data_available(DataReader reader) {
    TimeOfDayMessageDataReader messageReader = (TimeOfDayMessageDataReader) reader;
    TimeOfDayMessageSeq dataSeq = new TimeOfDayMessageSeq();
    SampleInfoSeq infoSeq = new SampleInfoSeq();

    try {
      messageReader.take(
          dataSeq,
          infoSeq,
          ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
          SampleStateKind.ANY_SAMPLE_STATE,
          ViewStateKind.ANY_VIEW_STATE,
          InstanceStateKind.ANY_INSTANCE_STATE);

      for (int i = 0; i < dataSeq.size(); i++) {
        SampleInfo info = (SampleInfo) infoSeq.get(i);
        if (info.valid_data) {
          TimeOfDayMessage ddsMsg = (TimeOfDayMessage) dataSeq.get(i);
          TimeOfDayEvent event = new TimeOfDayEvent(
              ddsMsg.timestamp, ddsMsg.messageId, ddsMsg.quote);
          inboundChannel.send(MessageBuilder.withPayload(event).build());
          log.debug("Forwarded from DDS: [{}]", event.getMessageId());
        }
      }
    } catch (RETCODE_NO_DATA noData) {
      // No data to read — not an error
    } finally {
      messageReader.return_loan(dataSeq, infoSeq);
    }
  }
}
