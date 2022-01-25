package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.adapter.json.newtokenerror.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.newtokenerror.JsonGenericTokenForNonexistentApplicantRequested;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenForNonexistentApplicantRequested;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenericTokenForNonexistentApplicantRequestedParser implements Parser<GenericTokenForNonexistentApplicantRequested> {

    public static GenericTokenForNonexistentApplicantRequestedParser getInstance() {
        return NewTokenForNonexistentApplicantRequestedEventParserSingletonContainer.SINGLETON;
    }

    protected static final class NewTokenForNonexistentApplicantRequestedEventParserSingletonContainer {
        protected static final GenericTokenForNonexistentApplicantRequestedParser SINGLETON =
            new GenericTokenForNonexistentApplicantRequestedParser();
    }

    @Override
    public ExternalEvent toExternalEvent(GenericTokenForNonexistentApplicantRequested genericTokenForNonexistentApplicantRequested) {
        return new JsonGenericTokenForNonexistentApplicantRequested(
            new JsonMeta(
                UUID.randomUUID().toString(),
                1,
                LocalDateTime.now().toString(),
                "NEW_TOKEN_FOR_NONEXISTENT_APPLICANT_REQUESTED"),
            new JsonBody(
                genericTokenForNonexistentApplicantRequested.getApplicantKey(),
                genericTokenForNonexistentApplicantRequested.getContestId(),
                genericTokenForNonexistentApplicantRequested.getLanguage()));
    }
}
