
package es.osoco.bbva.ats.forms.adapter.json.formanswered;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonFormConfig implements ExternalEvent {
    public Form form;
}
