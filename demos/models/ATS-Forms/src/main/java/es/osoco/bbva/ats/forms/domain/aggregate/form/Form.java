package es.osoco.bbva.ats.forms.domain.aggregate.form;

import es.osoco.bbva.ats.forms.domain.aggregate.AggregateRoot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
@Builder
public class Form implements AggregateRoot {
    private String formId;
    private String contestId;
    private String language;
    private String contestName;
    private Boolean canSaveDraft;
    private Boolean verifyApplicantEmail;
    private Boolean showAllowShareDataCheck;
    private RecoveryZone recoveryZone;
    private String sendingDataMessage;
    private ConfirmationMessage confirmationMessage;
    private TimeoutErrorMessage timeoutErrorMessage;
    private LegalConditions legalConditions;
    private Gdpr gdpr;
    private List<SummarySentence> summarySentence;
    private List<Section> sections;


    public String getId() {
        return this.formId + ":" + this.language.toUpperCase();
    }
}
