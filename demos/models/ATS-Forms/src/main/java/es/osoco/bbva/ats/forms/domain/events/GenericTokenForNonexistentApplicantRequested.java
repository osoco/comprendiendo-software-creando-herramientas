package es.osoco.bbva.ats.forms.domain.events;

public class GenericTokenForNonexistentApplicantRequested extends GenericTokenWithErrorRequested {

    public GenericTokenForNonexistentApplicantRequested(String applicantKey, String contestId, String language) {
        super(applicantKey, contestId, language);
    }
}
