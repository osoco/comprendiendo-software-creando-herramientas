package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class RecoveryTokenGenerated implements DomainEvent {

    private RecoveryToken recoveryToken;

}
