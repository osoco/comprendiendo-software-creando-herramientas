package es.osoco.bbva.ats.forms.domain.exception;

public class ApplicationToRecoverNotFoundException extends RecoverException {

    public ApplicationToRecoverNotFoundException(String applicantKey, String contestId, String language) {
        super(applicantKey, contestId, language);
    }
}
