package es.osoco.bbva.ats.forms.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class EmailToken implements AggregateRoot {

    private String language;

    private String contestId;

    private String applicantKey;

    private String emailToken;

    public String getId() {
        return applicantKey + ":" + emailToken;
    }
}
