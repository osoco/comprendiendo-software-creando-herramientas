package es.osoco.bbva.ats.forms.adapter;

import lombok.Getter;

@SuppressWarnings("unused")
public class RabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedAdapter
    extends AbstractRabbitMQPublisherAdapter
    implements Adapter<ExternalEvent> {

    public RabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedAdapter() {
    }

    private static RabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedAdapter rabbitMQApplicationSubmittedAdapter;

    public static RabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedAdapter getInstance() {
        return rabbitMQApplicationSubmittedAdapter;
    }

    static{
        rabbitMQApplicationSubmittedAdapter = new RabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedAdapter();
    }

    public RabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedAdapter(
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
