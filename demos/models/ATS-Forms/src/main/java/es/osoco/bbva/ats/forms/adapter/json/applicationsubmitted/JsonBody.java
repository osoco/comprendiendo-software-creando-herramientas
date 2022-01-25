
package es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class JsonBody {

    public Integer version;

    public String formId;

    public String contestId;

    public String applicantKey;

    public String applicationKey;

    public String recoveryKey;

    public String status;

    public String language;

    public Set<JsonAnswer> answers = null;

    public String origin;

    public String submissionDate;

    public String firstSubmissionDate;

    public String entityId;

}
