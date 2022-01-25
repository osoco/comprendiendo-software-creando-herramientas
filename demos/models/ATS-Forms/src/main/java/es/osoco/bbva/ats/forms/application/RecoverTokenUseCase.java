package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.JsonTokenRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.TokenRecoverRequested;

public class RecoverTokenUseCase extends UseCase<JsonTokenRecoverRequested> {

    public static RecoverTokenUseCase getInstance() {
        return RecoverTokenUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class RecoverTokenUseCaseSingletonContainer {
        protected static final RecoverTokenUseCase SINGLETON = new RecoverTokenUseCase();
    }

    @Override
    public void process(JsonTokenRecoverRequested applicationRecoverRequested) {
        domainInit(applicationRecoverRequested);

        TokenRecoverRequested tokenRecoverRequested = new TokenRecoverRequested(
                applicationRecoverRequested.getContestId(),
                applicationRecoverRequested.getLanguage(),
                applicationRecoverRequested.getApplicantKey().toLowerCase());

        domainEventService.subscribe(this);
        domainEventService.receive(tokenRecoverRequested);
    }
}
