package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplicationDraftStored extends ApplicationDomainEvent {
    public ApplicationDraftStored(Application application) {
        this.application = application;
    }
}
