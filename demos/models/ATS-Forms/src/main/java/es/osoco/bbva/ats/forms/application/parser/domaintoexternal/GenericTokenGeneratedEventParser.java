package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.adapter.json.newtoken.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.newtoken.JsonNewRecoveryTokenGenerated;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.aggregate.GenericToken;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenGenerated;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenericTokenGeneratedEventParser implements Parser<GenericTokenGenerated> {

    public static GenericTokenGeneratedEventParser getInstance() {
        return NewTokenGeneratedEventParserSingletonContainer.SINGLETON;
    }

    protected static final class NewTokenGeneratedEventParserSingletonContainer {
        protected static final GenericTokenGeneratedEventParser SINGLETON = new GenericTokenGeneratedEventParser();
    }

    @Override
    public ExternalEvent toExternalEvent(GenericTokenGenerated genericTokenGenerated) {

        GenericToken genericToken = genericTokenGenerated.getGenericToken();
        return new JsonNewRecoveryTokenGenerated(
                new JsonMeta(
                    UUID.randomUUID().toString(),
                    1,
                    LocalDateTime.now().toString(),
                    "NEW_TOKEN_GENERATED"),
                new JsonBody(
                    genericTokenGenerated.getRequestedContestId(),
                    genericToken.getLanguage(),
                    genericToken.getApplicantKey(),
                    genericToken.getToken()));
    }
}
