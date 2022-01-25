package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PartialApplicationRecovered implements DomainEvent {

    private Application application;
}
