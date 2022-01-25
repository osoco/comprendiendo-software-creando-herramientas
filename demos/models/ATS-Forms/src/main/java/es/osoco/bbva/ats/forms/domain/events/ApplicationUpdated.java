package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplicationUpdated extends ApplicationDomainEvent {
    public ApplicationUpdated(Application application) {
        this.application = application;
    }
}
