
package es.osoco.bbva.ats.forms.adapter.json.formanswered;


import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonEntityFormAnswered implements ExternalEvent {

    public String applicantKey;

    public String entityId;

    public Set<JsonAnswer> answers;

}
