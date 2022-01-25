package es.osoco.bbva.ats.forms.domain.events;


import lombok.Value;

@Value
public class ApplicationRecoverRequested implements DomainEvent {

    private String contestId;

    private String applicantKey;

    public String getID(){
        return contestId + ":" + applicantKey;
    }
}
