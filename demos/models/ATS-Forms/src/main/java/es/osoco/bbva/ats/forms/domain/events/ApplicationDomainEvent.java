package es.osoco.bbva.ats.forms.domain.events;


import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.Getter;

@Getter
public abstract class ApplicationDomainEvent implements DomainEvent {

    Application application;

}
