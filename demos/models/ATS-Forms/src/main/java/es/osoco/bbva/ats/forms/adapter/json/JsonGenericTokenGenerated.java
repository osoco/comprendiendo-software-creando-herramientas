package es.osoco.bbva.ats.forms.adapter.json;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonGenericTokenGenerated implements ExternalEvent {

    private String applicantKey;

    private Boolean userHasToken;
}
