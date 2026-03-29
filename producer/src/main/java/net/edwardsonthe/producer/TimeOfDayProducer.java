package net.edwardsonthe.producer;

import net.edwardsonthe.messages.TimeOfDayMessage;
import net.edwardsonthe.messages.TimeOfDayMessageDataWriter;
import net.edwardsonthe.messages.TimeOfDayMessageTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Publishes {@link TimeOfDayMessage} instances to a DDS topic at a fixed interval.
 *
 * <p>This is a standalone Java application (no framework) that manually creates all DDS
 * entities, runs a publish loop in {@code main()}, and registers a JVM shutdown hook for
 * cleanup. Each message contains an ISO 8601 timestamp, an incrementing message ID, and
 * a quote loaded from {@code quotes.txt} on the classpath.
 *
 * <p>Quotes are cycled sequentially; after the last quote is published, the cycle
 * restarts from the beginning.
 */
public class TimeOfDayProducer {

  private static final Logger logger = LoggerFactory.getLogger(TimeOfDayProducer.class);

  private static final int DOMAIN_ID = 0;
  private static final String TOPIC_NAME = "TimeOfDay";
  private static final int PUBLISH_INTERVAL_MS = 2000;

  private DomainParticipant participant;
  private TimeOfDayMessageDataWriter writer;
  private final List<String> quotes;
  private int quoteIndex = 0;
  private int messageId = 0;

  public TimeOfDayProducer() {
    this.quotes = loadQuotes();
  }

  /**
   * Initializes all DDS entities: DomainParticipant, type registration, Topic,
   * Publisher, and DataWriter. Must be called before {@link #publish()}.
   *
   * @throws RuntimeException if any DDS entity creation fails
   */
  public void start() {
    participant = DomainParticipantFactory.get_instance().create_participant(
        DOMAIN_ID,
        DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (participant == null) {
      throw new RuntimeException("Failed to create DomainParticipant");
    }
    logger.info("DDS DomainParticipant created on domain {}", DOMAIN_ID);

    TimeOfDayMessageTypeSupport.register_type(
        participant, TimeOfDayMessageTypeSupport.get_type_name());

    Topic topic = participant.create_topic(
        TOPIC_NAME,
        TimeOfDayMessageTypeSupport.get_type_name(),
        DomainParticipant.TOPIC_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (topic == null) {
      throw new RuntimeException("Failed to create Topic");
    }

    Publisher publisher = participant.create_publisher(
        DomainParticipant.PUBLISHER_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (publisher == null) {
      throw new RuntimeException("Failed to create Publisher");
    }

    writer = (TimeOfDayMessageDataWriter) publisher.create_datawriter(
        topic,
        Publisher.DATAWRITER_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    if (writer == null) {
      throw new RuntimeException("Failed to create DataWriter");
    }

    logger.info("Publishing on topic '{}'", TOPIC_NAME);
  }

  /**
   * Publishes a single {@link TimeOfDayMessage} with the current timestamp,
   * an incremented message ID, and the next quote in the cycle.
   */
  public void publish() {
    TimeOfDayMessage message = new TimeOfDayMessage();
    message.timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    message.messageId = ++messageId;
    message.quote = quotes.get(quoteIndex);
    quoteIndex = (quoteIndex + 1) % quotes.size();

    writer.write(message, InstanceHandle_t.HANDLE_NIL);
    logger.info("Published [{}]: {} — {}", message.messageId, message.timestamp, message.quote);
  }

  /**
   * Tears down all DDS entities in the correct order. Registered as a JVM
   * shutdown hook in {@link #main(String[])}.
   */
  public void shutdown() {
    logger.info("Shutting down producer");
    if (participant != null) {
      participant.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(participant);
    }
  }

  /**
   * Loads quotes from {@code /quotes.txt} on the classpath, one quote per line.
   * Blank lines are skipped.
   *
   * @return a non-empty list of quote strings
   * @throws RuntimeException if the file is not found, cannot be read, or is empty
   */
  private static List<String> loadQuotes() {
    List<String> lines = new ArrayList<>();
    InputStream is = TimeOfDayProducer.class.getResourceAsStream("/quotes.txt");
    if (is == null) {
      throw new RuntimeException("quotes.txt not found on classpath");
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          lines.add(line);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load quotes.txt", e);
    }
    if (lines.isEmpty()) {
      throw new RuntimeException("quotes.txt is empty");
    }
    logger.info("Loaded {} quotes", lines.size());
    return lines;
  }

  /**
   * Entry point. Creates the producer, registers a shutdown hook, initializes DDS,
   * and runs the publish loop until interrupted.
   *
   * @param args command-line arguments (unused)
   */
  public static void main(String[] args) {
    TimeOfDayProducer producer = new TimeOfDayProducer();

    Runtime.getRuntime().addShutdownHook(new Thread(producer::shutdown));

    producer.start();

    try {
      while (true) {
        producer.publish();
        Thread.sleep(PUBLISH_INTERVAL_MS);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Producer interrupted", e);
    }
  }
}
