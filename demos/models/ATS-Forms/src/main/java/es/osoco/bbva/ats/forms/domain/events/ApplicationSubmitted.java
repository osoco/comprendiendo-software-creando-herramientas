package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplicationSubmitted extends ApplicationDomainEvent {
    public ApplicationSubmitted(Application application) {
        this.application = application;
    }
}
