
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonBody {

    public String id;
    public Integer version;
    public String contestId;
    public String contestKey;
    public String formKey;
    public String contestName;
    public JsonLegalConditions legalConditions;
    public Boolean canSaveDraft;
    public Boolean verifyApplicantEmail;
    public List<String> languages = null;
    public String defaultLanguage;
    public Boolean showAllowShareDataCheck;
    public JsonRecoveryZone recoveryZone;
    public List<JsonI18n> sendingDataMessage = null;
    public JsonConfirmationMessage confirmationMessage;
    public JsonTimeoutErrorMessage timeoutErrorMessage;
    public JsonGdpr gdpr;
    public List<JsonSummarySentence> summarySentence = null;
    public List<JsonSection> sections = null;


}
