package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.EmailToken;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class EmailTokenGenerated implements DomainEvent {

    private EmailToken emailToken;

}
