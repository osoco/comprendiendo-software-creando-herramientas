package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.externaltodomain.ApplicationParser;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDraftSubmitted;

public class SubmitApplicationDraftUseCase extends UseCase<JsonFormAnswered> {

    public static SubmitApplicationDraftUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final SubmitApplicationDraftUseCase SINGLETON = new SubmitApplicationDraftUseCase();
    }

    @Override
    public void process(JsonFormAnswered formAnswered) {
        domainInit(formAnswered);

        ApplicationParser applicationParser = ApplicationParser.getInstance();
        Application application = applicationParser.createApplication(formAnswered, ApplicationStatus.DRAFT);
        ApplicationDraftSubmitted applicationDraftSubmitted = new ApplicationDraftSubmitted(application);

        domainEventService.subscribe(this);
        domainEventService.receive(applicationDraftSubmitted);
    }
}
