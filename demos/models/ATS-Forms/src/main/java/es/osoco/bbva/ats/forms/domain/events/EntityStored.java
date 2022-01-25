package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityStored extends ApplicationDomainEvent {
    public EntityStored(Application application) {
        this.application = application;
    }
}
