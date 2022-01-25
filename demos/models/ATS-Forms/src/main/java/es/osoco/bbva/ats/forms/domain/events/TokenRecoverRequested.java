package es.osoco.bbva.ats.forms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TokenRecoverRequested implements DomainEvent{
    private String contestId;

    private String language;

    private String applicantKey;

    public String getId(){
        return contestId + ":" + applicantKey;
    }
}
