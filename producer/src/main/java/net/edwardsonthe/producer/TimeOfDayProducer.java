package net.edwardsonthe.producer;

import net.edwardsonthe.common.TimeOfDayEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Publishes {@link TimeOfDayEvent} messages at a configurable interval.
 *
 * <p>This class has no dependency on any messaging transport (DDS, Kafka, etc.). It sends
 * {@link TimeOfDayEvent} objects to a Spring Integration {@link MessageChannel}. The
 * active Spring profile determines which channel adapter delivers the message to the
 * underlying transport.
 *
 * <p>Implements {@link CommandLineRunner} so the publish loop starts automatically
 * after the Spring context is fully initialized.
 */
@Component
public class TimeOfDayProducer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(TimeOfDayProducer.class);

  private final MessageChannel outboundChannel;
  private final Counter publishCounter;
  private final List<String> quotes;
  private int quoteIndex = 0;
  private int messageId = 0;

  @Value("${producer.publish-interval-ms:2000}")
  private int publishIntervalMs;

  /**
   * Constructs the producer with an outbound channel, meter registry, and quotes resource.
   *
   * @param outboundChannel the Spring Integration channel to send messages to
   * @param meterRegistry   the Micrometer registry for recording metrics
   * @param quotesFile      the resource containing quotes, one per line
   */
  public TimeOfDayProducer(@Qualifier("timeOfDayOutboundChannel") MessageChannel outboundChannel,
                           MeterRegistry meterRegistry,
                           @Value("${producer.quotes-file:classpath:quotes.txt}") Resource quotesFile) {
    this.outboundChannel = outboundChannel;
    this.publishCounter = Counter.builder("messages.published")
        .description("Total messages published")
        .register(meterRegistry);
    this.quotes = loadQuotes(quotesFile);
  }

  /**
   * Starts the publish loop. Runs continuously until the thread is interrupted
   * by Spring context shutdown.
   */
  @Override
  public void run(String... args) {
    log.info("Publishing every {}ms", publishIntervalMs);
    try {
      while (!Thread.currentThread().isInterrupted()) {
        publish();
        Thread.sleep(publishIntervalMs);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.info("Producer interrupted");
    }
  }

  /**
   * Publishes a single {@link TimeOfDayEvent} with the current timestamp,
   * an incremented message ID, and the next quote in the cycle.
   */
  private void publish() {
    TimeOfDayEvent event = new TimeOfDayEvent(
        DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
        ++messageId,
        quotes.get(quoteIndex));
    quoteIndex = (quoteIndex + 1) % quotes.size();

    outboundChannel.send(MessageBuilder.withPayload(event).build());
    publishCounter.increment();
    log.info("Published [{}]: {} — {}", event.getMessageId(), event.getTimestamp(), event.getQuote());
  }

  /**
   * Loads quotes from the given resource, one quote per line.
   * Blank lines are skipped.
   *
   * @param resource the Spring resource pointing to the quotes file
   * @return a non-empty list of quote strings
   * @throws RuntimeException if the file cannot be read or is empty
   */
  private static List<String> loadQuotes(Resource resource) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty()) {
          lines.add(line);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load quotes", e);
    }
    if (lines.isEmpty()) {
      throw new RuntimeException("Quotes file is empty");
    }
    log.info("Loaded {} quotes", lines.size());
    return lines;
  }
}
