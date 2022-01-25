package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.adapter.json.newtoken.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.newtoken.JsonNewRecoveryTokenGenerated;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.events.RecoveryTokenGenerated;

import java.time.LocalDateTime;
import java.util.UUID;

public class NewTokenGeneratedEventParser implements Parser<RecoveryTokenGenerated> {

    public static NewTokenGeneratedEventParser getInstance() {
        return NewTokenGeneratedEventParserSingletonContainer.SINGLETON;
    }

    protected static final class NewTokenGeneratedEventParserSingletonContainer {
        protected static final NewTokenGeneratedEventParser SINGLETON = new NewTokenGeneratedEventParser();
    }

    @Override
    public ExternalEvent toExternalEvent(RecoveryTokenGenerated recoveryTokenGenerated) {

        RecoveryToken recoveryToken = recoveryTokenGenerated.getRecoveryToken();
        return new JsonNewRecoveryTokenGenerated(
                new JsonMeta(
                    UUID.randomUUID().toString(),
                    1,
                    LocalDateTime.now().toString(),
                    "NEW_TOKEN_GENERATED"),
                new JsonBody(
                    recoveryToken.getContestId(),
                    recoveryToken.getLanguage(),
                    recoveryToken.getApplicantKey(),
                    recoveryToken.getRecoveryKey()));
    }
}
