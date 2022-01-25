package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;

public class ApplicationUpdateStored extends ApplicationDomainEvent {
    public ApplicationUpdateStored(Application application) {
        this.application = application;
    }
}
