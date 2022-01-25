package es.osoco.bbva.ats.forms.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class RecoveryToken implements AggregateRoot {

    private String contestId;

    private String language;

    private String applicantKey;

    private String recoveryKey;

    public String getId(){
        return contestId + ":" + applicantKey;
    }
}
