package es.osoco.bbva.ats.forms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericTokenWithErrorRequested implements DomainEvent {

    private String applicantKey;

    private String contestId;

    private String language;
}
