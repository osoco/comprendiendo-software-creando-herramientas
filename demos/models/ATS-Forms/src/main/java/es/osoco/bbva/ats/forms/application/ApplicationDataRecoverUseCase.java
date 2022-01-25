package es.osoco.bbva.ats.forms.application;


import es.osoco.bbva.ats.forms.adapter.ApplicationDataRecoverRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationDataRecoverRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.ApplicationDataRecoveredParser;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.PartialApplicationRecoveredParser;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDataRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDataRecovered;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;

public class ApplicationDataRecoverUseCase extends UseCase<JsonApplicationDataRecoverRequested> {

    public static ApplicationDataRecoverUseCase getInstance() {
        return ApplicationDataRecoverUseCase.ApplicationDataRecoverUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class ApplicationDataRecoverUseCaseSingletonContainer {
        protected static final ApplicationDataRecoverUseCase SINGLETON = new ApplicationDataRecoverUseCase();
    }

    @Override
    public void process(JsonApplicationDataRecoverRequested applicationDataRecoverRequested) {
        domainInit(applicationDataRecoverRequested);

        ApplicationDataRecoverRequested dataRecoverRequested = new ApplicationDataRecoverRequested(
                applicationDataRecoverRequested.getContestId(),
                applicationDataRecoverRequested.getApplicantKey().toLowerCase(),
                applicationDataRecoverRequested.getApplicantRecoveryKey());

        domainEventService.subscribe(this);
        domainEventService.receive(dataRecoverRequested);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        if (event instanceof PartialApplicationRecovered) {
            PartialApplicationRecovered partialApplicationRecovered = (PartialApplicationRecovered) event;
            JsonFormAnswered jsonFormAnswered = (JsonFormAnswered) PartialApplicationRecoveredParser.getInstance().toExternalEvent(partialApplicationRecovered);
            ApplicationDataRecoverRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonFormAnswered);
        }

        if (event instanceof ApplicationDataRecovered) {
            ApplicationDataRecovered applicationDataRecovered = (ApplicationDataRecovered) event;
            JsonFormAnswered jsonFormAnswered = (JsonFormAnswered) ApplicationDataRecoveredParser.getInstance().toExternalEvent(applicationDataRecovered);
            ApplicationDataRecoverRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonFormAnswered);
        }
    }
}

