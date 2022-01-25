
package es.osoco.bbva.ats.forms.domain.aggregate.form;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ConfirmationMessage {

    public String header;
    public String body;

}
