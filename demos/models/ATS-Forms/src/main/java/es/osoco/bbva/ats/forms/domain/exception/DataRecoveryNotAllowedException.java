package es.osoco.bbva.ats.forms.domain.exception;


public class DataRecoveryNotAllowedException extends RecoverException {
    public DataRecoveryNotAllowedException(String applicantKey, String contestId, String language) {
        super(applicantKey, contestId, language);
    }
}
