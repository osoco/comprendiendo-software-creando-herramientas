package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.JsonEmailTokenRequested;
import es.osoco.bbva.ats.forms.domain.events.EmailTokenRequested;

import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

public class EmailTokenUseCase extends UseCase<JsonEmailTokenRequested> {

    public static EmailTokenUseCase getInstance() {
        return EmailTokenUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class EmailTokenUseCaseSingletonContainer {
        protected static final EmailTokenUseCase SINGLETON = new EmailTokenUseCase();
    }

    @Override
    public void process(JsonEmailTokenRequested jsonEmailTokenRequested) {
        domainInit(jsonEmailTokenRequested);

        final Logging logging = LoggingFactory.getInstance().createLogging();
        logging.info("Processing use case...");

        EmailTokenRequested emailTokenRequested = new EmailTokenRequested(
                jsonEmailTokenRequested.getLanguage(),
                jsonEmailTokenRequested.getContestId(),
                jsonEmailTokenRequested.getApplicantKey().toLowerCase());

        domainEventService.subscribe(this);
        domainEventService.receive(emailTokenRequested);
    }
}
