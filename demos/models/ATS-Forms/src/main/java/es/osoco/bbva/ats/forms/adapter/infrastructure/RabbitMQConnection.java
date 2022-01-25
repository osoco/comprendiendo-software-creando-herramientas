package es.osoco.bbva.ats.forms.adapter.infrastructure;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import es.osoco.logging.LoggingFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnection {

		public static RabbitMQConnection getInstance() {
				if(RabbitMQConnection.INSTANCE == null) {
						RabbitMQConnection.build();
				}
				return RabbitMQConnection.INSTANCE;
		}

 		public Connection getConnection() {
				if ((connection == null) || (!connection.isOpen())) {
						createConnection();
				}
				return connection;
		}

		private static final String RABBITMQ_IP = System.getenv("RABBITMQ_IP");
		private static final String USER_NAME = System.getenv("RABBITMQ_USER_NAME");
		private static final String PASSWORD = System.getenv("RABBITMQ_PASSWORD");

		private static RabbitMQConnection INSTANCE = new RabbitMQConnection();
		private Connection connection;

		private RabbitMQConnection() {
				createConnection();
		}

		private void createConnection() {
				final ConnectionFactory factory = new ConnectionFactory();
				try {
						factory.setHost(RABBITMQ_IP);
						factory.setPort(5672);
						factory.setVirtualHost("/");
						factory.setUsername(USER_NAME);
						factory.setPassword(PASSWORD);
						factory.setRequestedHeartbeat(30);
						connection = factory.newConnection();
				} catch (IOException e) {
					LoggingFactory.getInstance().createLogging().error("RabbitMQ IO exception: " + e.getMessage());
				} catch (TimeoutException e) {
                    LoggingFactory.getInstance().createLogging().error("RabbitMQ time out exception: " + e.getMessage());
				}
		}

		private static void build() {
				RabbitMQConnection.INSTANCE = new RabbitMQConnection();
		}
}
