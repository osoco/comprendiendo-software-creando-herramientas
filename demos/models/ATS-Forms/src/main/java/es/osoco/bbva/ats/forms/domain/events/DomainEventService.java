package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.config.EventConsumerRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;


public class DomainEventService {

    @SuppressWarnings("unchecked")
    public void receive(final DomainEvent event) {
        // TODO - Log events received without any consumer found
        Optional<List<Consumer>> consumer = consumerRegistry.consumersForEvent(event);
        consumer.ifPresent(consumers -> consumers.forEach(con -> con.accept(event)));
    }

    public void notify(final DomainEvent event) {
        receive(event);
        listeners.forEach(listener -> listener.onEvent(event));
    }

    public void subscribe(final DomainEventListener listener) {
        listeners.add(listener);
    }

    private DomainEventService() {
        this.consumerRegistry = EventConsumerRegistry.getInstance();
        this.listeners = new HashSet<>();
    }

    protected static final class FormServiceEventServiceSingletonContainer {
        private static DomainEventService SINGLETON = new DomainEventService();
    }

    public static DomainEventService getInstance() {
        return FormServiceEventServiceSingletonContainer.SINGLETON;
    }

    public static DomainEventService getNewInstance() {
        FormServiceEventServiceSingletonContainer.SINGLETON = new DomainEventService();
        return getInstance();
    }

    private EventConsumerRegistry consumerRegistry;
    private Set<DomainEventListener> listeners;
}
