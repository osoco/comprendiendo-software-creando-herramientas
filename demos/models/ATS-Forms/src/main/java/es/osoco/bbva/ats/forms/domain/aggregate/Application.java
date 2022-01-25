package es.osoco.bbva.ats.forms.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.Map;


@Value
@AllArgsConstructor
@Builder
public class Application implements AggregateRoot {
    private Integer version;

    private String contestId;

    private String formId;

    private String origin;

    private String applicantKey;

    private String applicationKey;

    private ApplicationStatus status;

    private String language;

    private String recoveryKey;

    private ZonedDateTime submissionDate;

    private ZonedDateTime firstSubmissionDate;

    private Map<String, Answer> answersById;

    private String entityId;

    public String getLanguage() {
        return this.language.toUpperCase();
    }

    public String getId() {
        return this.contestId + ":" + this.applicantKey;
    }

    public String getEmailToken() {
        return getToken(EMAIL_TOKEN_QUESTION_ID);
    }

    public String getFormToken() {
        return getToken(FORM_TOKEN_QUESTION_ID);
    }

    public void removeTokens() {
        removeFormToken();
        removeEmailToken();
    }

    private void removeFormToken() {
        answersById.remove(FORM_TOKEN_QUESTION_ID);
    }

    private void removeEmailToken() {
        answersById.remove(EMAIL_TOKEN_QUESTION_ID);
    }

    private String getToken(String questionId) {
        String token = null;
        Answer tokenAnswer = answersById.get(questionId);
        if (tokenAnswer != null) {
           token = tokenAnswer.getText();
        }
        return token;
    }


    public static final String EMAIL_TOKEN_QUESTION_ID = "__email-token";
    public static final String FORM_TOKEN_QUESTION_ID = "__form-token";

}
