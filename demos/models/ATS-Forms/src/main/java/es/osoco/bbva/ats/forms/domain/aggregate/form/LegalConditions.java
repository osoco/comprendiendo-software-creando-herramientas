
package es.osoco.bbva.ats.forms.domain.aggregate.form;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class LegalConditions {

    public String link;
    public String text;
    public String linkText;

}
