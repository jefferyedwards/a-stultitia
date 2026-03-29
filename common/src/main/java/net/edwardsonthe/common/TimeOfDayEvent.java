package net.edwardsonthe.common;

/**
 * Transport-neutral representation of a time-of-day message.
 *
 * <p>This POJO carries the same fields as the DDS {@code TimeOfDayMessage} but has
 * no dependency on RTI or any messaging framework. Business logic works exclusively
 * with this type. The conversion to/from transport-specific formats (DDS, Kafka, etc.)
 * is handled by the channel adapters in the transport modules.
 */
public class TimeOfDayEvent {

  private String timestamp;
  private int messageId;
  private String quote;

  public TimeOfDayEvent() {
  }

  public TimeOfDayEvent(String timestamp, int messageId, String quote) {
    this.timestamp = timestamp;
    this.messageId = messageId;
    this.quote = quote;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getMessageId() {
    return messageId;
  }

  public void setMessageId(int messageId) {
    this.messageId = messageId;
  }

  public String getQuote() {
    return quote;
  }

  public void setQuote(String quote) {
    this.quote = quote;
  }

  @Override
  public String toString() {
    return "TimeOfDayEvent{timestamp='" + timestamp + "', messageId=" + messageId
        + ", quote='" + quote + "'}";
  }
}
