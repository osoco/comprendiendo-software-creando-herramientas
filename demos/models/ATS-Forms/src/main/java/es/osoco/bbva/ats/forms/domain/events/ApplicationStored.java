package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplicationStored extends ApplicationDomainEvent {
    public ApplicationStored(Application application) {
        this.application = application;
    }
}
