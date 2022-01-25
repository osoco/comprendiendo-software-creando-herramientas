package es.osoco.bbva.ats.forms.domain.events;

import lombok.Value;

@Value
public class ApplicationRecoverWithRecoveryKeyRequested implements DomainEvent {

    private String contestId;

    private String applicantKey;

    private String recoveryKey;

    public String getID(){
        return contestId + ":" + applicantKey;
    }
}
