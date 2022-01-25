package es.osoco.bbva.ats.forms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class VerifyEmailTokenRequested implements DomainEvent {

    private String emailToken;

}
