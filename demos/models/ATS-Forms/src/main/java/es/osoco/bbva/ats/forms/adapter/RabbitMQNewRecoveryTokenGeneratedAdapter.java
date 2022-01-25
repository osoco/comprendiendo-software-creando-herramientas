package es.osoco.bbva.ats.forms.adapter;

import lombok.Getter;

@SuppressWarnings("unused")
public class RabbitMQNewRecoveryTokenGeneratedAdapter
    extends AbstractRabbitMQPublisherAdapter
    implements Adapter<ExternalEvent> {

    public RabbitMQNewRecoveryTokenGeneratedAdapter() {
    }

    private static RabbitMQNewRecoveryTokenGeneratedAdapter rabbitMQApplicationSubmittedAdapter;

    public static RabbitMQNewRecoveryTokenGeneratedAdapter getInstance() {
        return rabbitMQApplicationSubmittedAdapter;
    }

    static {
        rabbitMQApplicationSubmittedAdapter = new RabbitMQNewRecoveryTokenGeneratedAdapter();
    }

    public RabbitMQNewRecoveryTokenGeneratedAdapter(
        final String exchange, final String routingKey) {

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
