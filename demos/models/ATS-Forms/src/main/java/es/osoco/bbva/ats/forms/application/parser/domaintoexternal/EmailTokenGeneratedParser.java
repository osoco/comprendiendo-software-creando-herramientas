package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.adapter.json.emailtoken.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.emailtoken.JsonEmailTokenGenerated;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.aggregate.EmailToken;
import es.osoco.bbva.ats.forms.domain.events.EmailTokenGenerated;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailTokenGeneratedParser implements Parser<EmailTokenGenerated> {

    public static EmailTokenGeneratedParser getInstance() {
        return EmailTokenGeneratedParserSingletonContainer.SINGLETON;
    }

    protected static final class EmailTokenGeneratedParserSingletonContainer {
        protected static final EmailTokenGeneratedParser SINGLETON = new EmailTokenGeneratedParser();
    }

    @Override
    public ExternalEvent toExternalEvent(EmailTokenGenerated emailTokenGenerated) {

        EmailToken emailToken = emailTokenGenerated.getEmailToken();
        return new JsonEmailTokenGenerated(
                new JsonMeta(
                    UUID.randomUUID().toString(),
                    1,
                    LocalDateTime.now().toString(),
                    "EMAIL_TOKEN_GENERATED"),
                new JsonBody(
                    emailToken.getLanguage(),
                    emailToken.getContestId(),
                    emailToken.getApplicantKey(),
                    emailToken.getEmailToken()));
    }
}
