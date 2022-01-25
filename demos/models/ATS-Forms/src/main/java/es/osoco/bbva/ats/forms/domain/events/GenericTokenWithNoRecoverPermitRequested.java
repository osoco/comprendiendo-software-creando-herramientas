package es.osoco.bbva.ats.forms.domain.events;

public class GenericTokenWithNoRecoverPermitRequested extends GenericTokenWithErrorRequested {

    public GenericTokenWithNoRecoverPermitRequested(String applicantKey, String contestId, String language) {
        super(applicantKey, contestId, language);
    }
}
