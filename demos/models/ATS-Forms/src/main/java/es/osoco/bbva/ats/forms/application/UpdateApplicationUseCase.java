package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.externaltodomain.ApplicationParser;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdated;

public class UpdateApplicationUseCase extends UseCase<JsonFormAnswered> {

    public static UpdateApplicationUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final UpdateApplicationUseCase SINGLETON = new UpdateApplicationUseCase();
    }

    @Override
    public void process(JsonFormAnswered formAnswered) {
        domainInit(formAnswered);

        ApplicationParser applicationParser = ApplicationParser.getInstance();
        Application application = applicationParser.createApplication(formAnswered, null);
        ApplicationUpdated applicationSubmitted = new ApplicationUpdated(application);

        domainEventService.subscribe(this);
        domainEventService.receive(applicationSubmitted);
    }
}
