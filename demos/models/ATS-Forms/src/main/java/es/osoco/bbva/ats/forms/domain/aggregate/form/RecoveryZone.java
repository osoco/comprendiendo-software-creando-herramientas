package es.osoco.bbva.ats.forms.domain.aggregate.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class RecoveryZone {
    private String title;
    private String message;
    private String button;
    private Checkbox checkbox;
}
