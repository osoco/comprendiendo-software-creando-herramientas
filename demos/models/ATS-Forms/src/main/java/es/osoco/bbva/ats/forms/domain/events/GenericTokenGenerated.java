package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.GenericToken;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class GenericTokenGenerated implements DomainEvent {

    private GenericToken genericToken;

    private String requestedContestId;

    private String recoveryContestId;

    private Boolean userHasToken;
}
