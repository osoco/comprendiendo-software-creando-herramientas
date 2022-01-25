package es.osoco.bbva.ats.forms.domain.aggregate.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class Checkbox {
    private String helpText;
    private String label;
    private String errorLabel;
}
