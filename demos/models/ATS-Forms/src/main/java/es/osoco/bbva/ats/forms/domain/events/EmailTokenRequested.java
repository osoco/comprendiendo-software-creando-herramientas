package es.osoco.bbva.ats.forms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class EmailTokenRequested implements DomainEvent {

    private String language;

    private String contestId;

    private String applicantKey;

}
