package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.adapter.json.newtokenerror.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.newtokenerror.JsonGenericTokenWithNoRecoverPermitRequested;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenWithNoRecoverPermitRequested;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenericTokenWithNoRecoverPermitRequestedParser implements Parser<GenericTokenWithNoRecoverPermitRequested> {

    public static GenericTokenWithNoRecoverPermitRequestedParser getInstance() {
        return NewTokenWithNoRecoverPermitRequestedEventParserSingletonContainer.SINGLETON;
    }

    protected static final class NewTokenWithNoRecoverPermitRequestedEventParserSingletonContainer {
        protected static final GenericTokenWithNoRecoverPermitRequestedParser SINGLETON =
            new GenericTokenWithNoRecoverPermitRequestedParser();
    }

    @Override
    public ExternalEvent toExternalEvent(GenericTokenWithNoRecoverPermitRequested genericTokenWithNoRecoverPermitRequested) {
        return new JsonGenericTokenWithNoRecoverPermitRequested(
            new JsonMeta(
                UUID.randomUUID().toString(),
                1,
                LocalDateTime.now().toString(),
                "NEW_TOKEN_WITH_NO_RECOVER_PERMIT_REQUESTED"),
            new JsonBody(
                genericTokenWithNoRecoverPermitRequested.getApplicantKey(),
                genericTokenWithNoRecoverPermitRequested.getContestId(),
                genericTokenWithNoRecoverPermitRequested.getLanguage()));
    }
}
