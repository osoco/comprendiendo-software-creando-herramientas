package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.externaltodomain.ApplicationParser;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus;
import es.osoco.bbva.ats.forms.domain.events.EntitySubmitted;

public class SubmitEntityUseCase extends UseCase<JsonFormAnswered> {


    public static SubmitEntityUseCase getInstance() {
        return SubmitFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class SubmitFormUseCaseSingletonContainer {
        protected static final SubmitEntityUseCase SINGLETON = new SubmitEntityUseCase();
    }

    @Override
    public void process(JsonFormAnswered formAnswered) {
        domainInit(formAnswered);

        ApplicationParser applicationParser = ApplicationParser.getInstance();
        Application application = applicationParser.createApplication(formAnswered, ApplicationStatus.FINISHED);
        EntitySubmitted entitySubmitted = new EntitySubmitted(application);

        domainEventService.subscribe(this);
        domainEventService.receive(entitySubmitted);
    }

}
