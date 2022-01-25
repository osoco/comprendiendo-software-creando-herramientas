package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.EntityRecoverRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonEntityRecoverRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonEntityFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.EntityRecoveredParser;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.EntityRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.EntityRecovered;

public class RecoverEntityUseCase extends UseCase<JsonEntityRecoverRequested> {

    public static RecoverEntityUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final RecoverEntityUseCase SINGLETON = new RecoverEntityUseCase();
    }

    @Override
    public void process(JsonEntityRecoverRequested entityRecoverRequested) {
        domainInit(entityRecoverRequested);

        EntityRecoverRequested recoverRequested = new EntityRecoverRequested(
            entityRecoverRequested.getExternalId(),
            entityRecoverRequested.getApplicantKey().toLowerCase()
        );

        domainEventService.subscribe(this);
        domainEventService.receive(recoverRequested);
    }

    @Override
    public void onEvent(final DomainEvent event) {
        if (event instanceof EntityRecovered) {
            EntityRecovered entityRecovered = (EntityRecovered) event;
            JsonEntityFormAnswered jsonEntityFormAnswered = (JsonEntityFormAnswered) EntityRecoveredParser.getInstance().toExternalEvent(entityRecovered);
            EntityRecoverRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonEntityFormAnswered);
        }
    }
}

