package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated;
import es.osoco.bbva.ats.forms.application.parser.externaltodomain.FormParser;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.events.FormUpdated;

import java.util.List;

public class UpdateFormUseCase extends UseCase<JsonFormUpdated> {

    public static UpdateFormUseCase getInstance() {
        return UpdateFormUseCase.UpdateFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class UpdateFormUseCaseSingletonContainer {
        protected static final UpdateFormUseCase SINGLETON = new UpdateFormUseCase();
    }

    @Override
    public void process(JsonFormUpdated formUpdated) {
        domainInit(formUpdated);

        FormParser formParser = FormParser.getInstance();
        System.out.println("Parsing form updated or created event...");
        List<Form> forms = formParser.createForms(formUpdated);


        domainEventService.subscribe(this);
        forms.forEach(form -> {
            FormUpdated formUpdatedDomainEvent = new FormUpdated(form);
            domainEventService.receive(formUpdatedDomainEvent);
        });
    }
}
