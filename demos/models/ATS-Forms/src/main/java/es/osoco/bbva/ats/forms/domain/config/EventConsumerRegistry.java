package es.osoco.bbva.ats.forms.domain.config;

import es.osoco.bbva.ats.forms.domain.events.ApplicationDataRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDraftSubmitted;
import es.osoco.bbva.ats.forms.domain.events.ApplicationRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.ApplicationRecoverWithRecoveryKeyRequested;
import es.osoco.bbva.ats.forms.domain.events.ApplicationSubmitted;
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdated;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.EntityRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.EntitySubmitted;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenRequested;
import es.osoco.bbva.ats.forms.domain.events.TokenRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.FormConfigRequested;
import es.osoco.bbva.ats.forms.domain.service.ApplicationDataRecoverRequestedService;
import es.osoco.bbva.ats.forms.domain.service.ApplicationDraftSubmittedService;
import es.osoco.bbva.ats.forms.domain.service.ApplicationRecoverRequestedService;
import es.osoco.bbva.ats.forms.domain.service.ApplicationRecoverWithRecoveryKeyRequestedService;
import es.osoco.bbva.ats.forms.domain.service.ApplicationSubmittedService;
import es.osoco.bbva.ats.forms.domain.service.ApplicationUpdatedService;
import es.osoco.bbva.ats.forms.domain.service.EntityRecoverRequestedService;
import es.osoco.bbva.ats.forms.domain.service.EntitySubmittedService;
import es.osoco.bbva.ats.forms.domain.service.GenericTokenRequestedService;
import es.osoco.bbva.ats.forms.domain.service.TokenRecoverRequestedService;
import es.osoco.bbva.ats.forms.domain.service.FormConfigRequestedService;
import es.osoco.bbva.ats.forms.domain.service.EmailTokenRequestedService;
import es.osoco.bbva.ats.forms.domain.service.VerifyEmailTokenRequestedService;
import es.osoco.bbva.ats.forms.domain.events.*;
import es.osoco.bbva.ats.forms.domain.service.*;

import java.util.*;
import java.util.function.Consumer;


public class EventConsumerRegistry {

    public Optional<List<Consumer>> consumersForEvent(final DomainEvent event) {
		    return consumersForEventType(event.getClass());
    }

    private Optional<List<Consumer>> consumersForEventType(final Class eventClass) {
        return Optional.ofNullable(registry.get(eventClass));
    }

    private static class EventConsumerRegistrySingletonContainer {
        private static final EventConsumerRegistry SINGLETON = new EventConsumerRegistry();
    }

    public static EventConsumerRegistry getInstance() {
        return EventConsumerRegistrySingletonContainer.SINGLETON;
    }

    private final Map<Class, List<Consumer>> registry;

    private EventConsumerRegistry() {
        registry = new HashMap<>();
        addConsumerForEvent(new ApplicationSubmittedService(), ApplicationSubmitted.class);
        addConsumerForEvent(new ApplicationDraftSubmittedService(), ApplicationDraftSubmitted.class);
        addConsumerForEvent(new ApplicationRecoverRequestedService(), ApplicationRecoverRequested.class);
        addConsumerForEvent(new ApplicationRecoverWithRecoveryKeyRequestedService(), ApplicationRecoverWithRecoveryKeyRequested.class);
        addConsumerForEvent(new TokenRecoverRequestedService(), TokenRecoverRequested.class);
        addConsumerForEvent(new EmailTokenRequestedService(), EmailTokenRequested.class);
        addConsumerForEvent(new VerifyEmailTokenRequestedService(), VerifyEmailTokenRequested.class);
        addConsumerForEvent(new ApplicationUpdatedService(), ApplicationUpdated.class);
        addConsumerForEvent(new GenericTokenRequestedService(), GenericTokenRequested.class);
        addConsumerForEvent(new ApplicationDataRecoverRequestedService(), ApplicationDataRecoverRequested.class );
        addConsumerForEvent(new EntityRecoverRequestedService(), EntityRecoverRequested.class);
        addConsumerForEvent(new EntitySubmittedService(), EntitySubmitted.class);
        addConsumerForEvent(new FormConfigRequestedService(), FormConfigRequested.class);
        addConsumerForEvent(new FormUpdatedService(), FormUpdated.class);
    }

    private <T extends DomainEvent> void addConsumerForEvent(
        final Consumer<T> consumer, final Class<T> eventClass) {

        List<Consumer> consumers = registry.computeIfAbsent(eventClass, k -> new ArrayList<>());
        consumers.add(consumer);
    }
}
