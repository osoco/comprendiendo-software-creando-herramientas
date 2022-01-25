package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.VerifyEmailTokenApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonEmailTokenVerified;
import es.osoco.bbva.ats.forms.adapter.json.JsonVerifyEmailTokenRequested;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.EmailTokenVerified;
import es.osoco.bbva.ats.forms.domain.events.VerifyEmailTokenRequested;

import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

public class VerifyEmailTokenUseCase extends UseCase<JsonVerifyEmailTokenRequested> {

    public static VerifyEmailTokenUseCase getInstance() {
        return VerifyEmailTokenUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class VerifyEmailTokenUseCaseSingletonContainer {
        protected static final VerifyEmailTokenUseCase SINGLETON = new VerifyEmailTokenUseCase();
    }

    @Override
    public void process(JsonVerifyEmailTokenRequested jsonVerifyEmailTokenRequested) {
        domainInit(jsonVerifyEmailTokenRequested);

        final Logging logging = LoggingFactory.getInstance().createLogging();

        VerifyEmailTokenRequested verifyEmailTokenRequested = new VerifyEmailTokenRequested(
                jsonVerifyEmailTokenRequested.getEmailToken());

        domainEventService.subscribe(this);
        domainEventService.receive(verifyEmailTokenRequested);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        final Logging logging = LoggingFactory.getInstance().createLogging();
        logging.info("Domain event generated: " + event);

        if (event instanceof EmailTokenVerified) {
            EmailTokenVerified emailTokenVerified = (EmailTokenVerified) event;

            createAndSendJsonEmailTokenVerified(
                emailTokenVerified.getApplicantKey()
            );
        }
    }

    private void createAndSendJsonEmailTokenVerified(String applicantKey) {
        JsonEmailTokenVerified jsonEmailTokenVerified =
            new JsonEmailTokenVerified(applicantKey);

        VerifyEmailTokenApiGatewayAdapter.getInstance().onOutputEvent(jsonEmailTokenVerified);
    }

}
