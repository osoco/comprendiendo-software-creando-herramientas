package es.osoco.bbva.ats.forms.adapter.infrastructure;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import es.osoco.bbva.ats.forms.adapter.FormUpdatedRabbitMqAdapter;
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationProducer;

import java.io.IOException;
import java.lang.reflect.Type;

public class RabbitMQConsumer implements RequestHandler<ScheduledEvent, String> {

    public static RabbitMQConsumer getInstance() {
        return RabbitMQConsumer.RabbitMQConsumerSingletonContainer.SINGLETON;
    }

    protected static final class RabbitMQConsumerSingletonContainer {
        protected  static RabbitMQConsumer SINGLETON = new RabbitMQConsumer();
    }

    public RabbitMQConsumer() {
        RabbitMQConsumer.RabbitMQConsumerSingletonContainer.SINGLETON = this;
    }


    private static final String QUEUE_NAME = System.getenv("QUEUE_NAME");

	private void consume() throws IOException {
		Connection connection = RabbitMQConnection.getInstance().getConnection();

		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, true , false, false, null);
/*
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
					throws IOException {

                System.out.println("Consuming form created or updated message...");
				String message = new String(body, "UTF-8");
                Gson gson = new Gson();
                Type type = new TypeToken<JsonFormUpdated>() {}.getType();
                JsonFormUpdated jsonFormUpdated = gson.fromJson(message, type);


                //TODO improve with factory when consumes more than one type of messages
                FormUpdatedRabbitMqAdapter formUpdatedRabbitMqAdapter = new FormUpdatedRabbitMqAdapter();
                formUpdatedRabbitMqAdapter.onInputEvent(jsonFormUpdated);
			}
		};
        System.out.println("RabbitMQ consumer created sucessfully");


        channel.basicConsume(QUEUE_NAME, true, consumer);
        */

        GetResponse getResponse = channel.basicGet(QUEUE_NAME, true);
        if (getResponse != null){
            byte[] body = getResponse.getBody();

            System.out.println("Consuming form created or updated message...");
            String message = new String(body, "UTF-8");
            System.out.println("Message consumed: " + message);
            Gson gson = new Gson();
            Type type = new TypeToken<JsonFormUpdated>() {}.getType();
            JsonFormUpdated jsonFormUpdated = gson.fromJson(message, type);
            FormUpdatedRabbitMqAdapter formUpdatedRabbitMqAdapter = new FormUpdatedRabbitMqAdapter();
            formUpdatedRabbitMqAdapter.onInputEvent(jsonFormUpdated);
        }
        channel.abort();
        connection.close();
	}

	@Override
	public String handleRequest(ScheduledEvent request, Context context) {
		try {
            initLogger(context);
            System.out.println("incoming request: " + request.toString());
            this.consume();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "OK";
	}

    private void initLogger(final Context context) {
        new AwsLambdaLoggingConfigurationProducer().configureLogging(context.getLogger());
    }

}
