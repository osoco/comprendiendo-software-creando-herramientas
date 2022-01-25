package es.osoco.bbva.ats.forms.adapter.infrastructure;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import es.osoco.logging.LoggingFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

public class RabbitMQPublisher {

	static public RabbitMQPublisher getInstance() {
			return new RabbitMQPublisher();
	}
	public void publishMessage(
			final String exchange,
			final String routingKey,
			final String json)
			throws IOException, TimeoutException {

			LocalDateTime start = LocalDateTime.now();

			final Connection connection = RabbitMQConnection.getInstance().getConnection();
			final Channel channel = connection.createChannel();

			AMQP.BasicProperties rabbitBasicProperties = new AMQP.BasicProperties().builder()
							.deliveryMode(2)
							.contentType("application/json")
							.build();

			channel.basicPublish(exchange, routingKey, rabbitBasicProperties, json.getBytes(StandardCharsets.UTF_8));

			channel.close();
		LoggingFactory.getInstance().createLogging().info("### RabbitMQ published events in: " + Duration.between(start, LocalDateTime.now()));
	}
}
