package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.externaltodomain.ApplicationParser;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus;
import es.osoco.bbva.ats.forms.domain.events.ApplicationSubmitted;

public class SubmitApplicationUseCase extends UseCase<JsonFormAnswered> {

    public static SubmitApplicationUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final SubmitApplicationUseCase SINGLETON = new SubmitApplicationUseCase();
    }

    @Override
    public void process(JsonFormAnswered formAnswered) {
        domainInit(formAnswered);

        ApplicationParser applicationParser = ApplicationParser.getInstance();
        Application application = applicationParser.createApplication(formAnswered, ApplicationStatus.FINISHED);
        ApplicationSubmitted applicationSubmitted = new ApplicationSubmitted(application);

        domainEventService.subscribe(this);
        domainEventService.receive(applicationSubmitted);
    }
}
