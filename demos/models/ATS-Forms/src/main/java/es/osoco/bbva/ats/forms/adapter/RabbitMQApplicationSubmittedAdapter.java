package es.osoco.bbva.ats.forms.adapter;

import lombok.Getter;

@SuppressWarnings("unused")
public class RabbitMQApplicationSubmittedAdapter
    extends AbstractRabbitMQPublisherAdapter
    implements Adapter<ExternalEvent> {

    public RabbitMQApplicationSubmittedAdapter() {
    }

    private static RabbitMQApplicationSubmittedAdapter rabbitMQApplicationSubmittedAdapter;

    public static RabbitMQApplicationSubmittedAdapter getInstance() {
        return rabbitMQApplicationSubmittedAdapter;
    }

    static{
        rabbitMQApplicationSubmittedAdapter = new RabbitMQApplicationSubmittedAdapter();
    }

    public RabbitMQApplicationSubmittedAdapter(final String exchange, final String routingKey) {
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void onOutputEvent(ExternalEvent outputEvent) {
        publishEvent(getParser().toJson(outputEvent));
    }

    @Override
    public void onInputEvent(ExternalEvent outputEvent) {

    }

    @Getter
    private String exchange = System.getenv("APPLICATION_SUBMITTED_EXCHANGE");
    @Getter
    private String routingKey = System.getenv("APPLICATION_SUBMITTED_ROUTING_KEY");
}
