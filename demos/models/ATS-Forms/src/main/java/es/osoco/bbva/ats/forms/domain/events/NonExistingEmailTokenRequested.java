package es.osoco.bbva.ats.forms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NonExistingEmailTokenRequested implements DomainEvent {

    private String emailToken;

}
