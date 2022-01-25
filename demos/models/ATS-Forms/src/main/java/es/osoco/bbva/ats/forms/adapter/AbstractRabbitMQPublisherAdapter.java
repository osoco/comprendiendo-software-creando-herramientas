package es.osoco.bbva.ats.forms.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.RabbitMQPublisher;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeSerializer;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;
import lombok.Getter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

/**
 * Common logic for adapters publishing messages to RabbitMQ.
 */
public abstract class AbstractRabbitMQPublisherAdapter {

    /**
     * Publishes an event to a RabbitMQ exchange.
     * @param jsonEvent the event to publish.
     */
    protected void publishEvent(final String jsonEvent) {
        final Logging logging = LoggingFactory.getInstance().createLogging();

        logging.info("publishing events: " + jsonEvent);
        if (jsonEvent == null) {
            logging.error("Could not parse the events: Unknown Event " + jsonEvent);
        } else {
            try {
                LocalDateTime start = LocalDateTime.now();
                RabbitMQPublisher.getInstance().publishMessage(getExchange(), getRoutingKey(), jsonEvent);
                logging.info("### RabbitMQ published events in: " + Duration.between(start, LocalDateTime.now()));
            } catch (IOException | TimeoutException e) {
                logging.error(e.getMessage());
            }
        }
    }

    /**
     * Retrieves the name of the exchange.
     * @return such name.
     */
    public abstract String getExchange();

    /**
     * Retrieves the routing key.
     * @return the routing key.
     */
    public abstract String getRoutingKey();

     Gson getParser() {
        return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer()).create();
    }
}
