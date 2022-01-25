package es.osoco.bbva.ats.forms.domain.events;

import lombok.Value;

@Value
public class FormConfigRequested implements DomainEvent {
    private String formId;
    private String language;
}
