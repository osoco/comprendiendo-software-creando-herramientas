package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.GenericTokenRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonGenericTokenGenerated;
import es.osoco.bbva.ats.forms.adapter.json.JsonGenericTokenRequested;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenForNonexistentApplicantRequested;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenGenerated;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenRequested;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenWithErrorRequested;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenWithNoRecoverPermitRequested;

//Tal ver renombrar a GenerateTokenUseCase
public class GenerateGenericTokenUseCase extends UseCase<JsonGenericTokenRequested> {

    public static GenerateGenericTokenUseCase getInstance() {
        return GenerateGenericTokenUseCase.GenerateGenericTokenUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class GenerateGenericTokenUseCaseSingletonContainer {
        protected static final GenerateGenericTokenUseCase SINGLETON = new GenerateGenericTokenUseCase();
    }

    @Override
    public void process(JsonGenericTokenRequested jsonGenericTokenRequested) {
        domainInit(jsonGenericTokenRequested);

        if (jsonGenericTokenRequested.getApplicantKey() != null) {
            GenericTokenRequested genericTokenRequested = new GenericTokenRequested(
                jsonGenericTokenRequested.getContestId(),
                jsonGenericTokenRequested.getLanguage(),
                jsonGenericTokenRequested.getApplicantKey().toLowerCase(),
                jsonGenericTokenRequested.getAllowRecoverData(),
                jsonGenericTokenRequested.getAllowRecoverDataText(),
                jsonGenericTokenRequested.getForceNewToken()
            );

            domainEventService.subscribe(this);
            domainEventService.receive(genericTokenRequested);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        if (event instanceof GenericTokenGenerated) {
            GenericTokenGenerated genericTokenGenerated = (GenericTokenGenerated) event;

            createAndSendJsonGenericTokenGenerated(
                genericTokenGenerated.getGenericToken().getApplicantKey(),
                genericTokenGenerated.getUserHasToken()
            );

            super.onEvent(event);
        }

        if (event instanceof GenericTokenWithErrorRequested) {
            GenericTokenWithErrorRequested genericTokenWithErrorRequested =
                (GenericTokenWithErrorRequested) event;

            createAndSendJsonGenericTokenGenerated(
                genericTokenWithErrorRequested.getApplicantKey(),
                false
            );

            super.onEvent(event);
        }
    }

    private void createAndSendJsonGenericTokenGenerated(String applicantKey, Boolean userHasToken) {
        JsonGenericTokenGenerated jsonGenericTokenGenerated = new JsonGenericTokenGenerated(
            applicantKey,
            userHasToken
        );
        
        GenericTokenRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonGenericTokenGenerated);
    }
}
