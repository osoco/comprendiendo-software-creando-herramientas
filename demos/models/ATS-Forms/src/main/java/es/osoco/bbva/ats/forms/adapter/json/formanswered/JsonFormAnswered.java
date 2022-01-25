
package es.osoco.bbva.ats.forms.adapter.json.formanswered;


import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonFormAnswered implements ExternalEvent {

    public String contestId;

    public String formId;

    public String language;

    public String status;

    public String submissionDate;

    public String entityId;

    public Set<JsonAnswer> answers = null;

    public String origin;

}
