package es.osoco.bbva.ats.forms.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class GenericToken implements AggregateRoot {

    private String language;

    private String applicantKey;

    private String token;

}
