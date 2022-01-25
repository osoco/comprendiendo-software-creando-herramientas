package es.osoco.bbva.ats.forms.domain.events;


import lombok.Value;

@Value
public class EntityRecoverRequested implements DomainEvent {

    private String externalId;
    private String applicantKey;
}
