package es.osoco.bbva.ats.forms.domain.events;

import java.io.Serializable;

public interface DomainEvent extends Serializable {

    default void emit() {
        DomainEventService eventService = DomainEventService.getInstance();
        eventService.notify(this);
    }
}
