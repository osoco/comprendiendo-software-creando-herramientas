package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.FormConfigRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.json.JsonFormConfigRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormConfig;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.FormConfigLoadedParser;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.FormConfigLoaded;
import es.osoco.bbva.ats.forms.domain.events.FormConfigRequested;

public class LoadFormConfigUseCase extends UseCase<JsonFormConfigRequested> {

    public static LoadFormConfigUseCase getInstance() {
        return LoadFormConfigUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class LoadFormConfigUseCaseSingletonContainer {
        protected static final LoadFormConfigUseCase SINGLETON = new LoadFormConfigUseCase();
    }

    @Override
    public void process(JsonFormConfigRequested jsonFormConfigRequested) {
        domainInit(jsonFormConfigRequested);

        FormConfigRequested formConfigRequested = new FormConfigRequested(
            jsonFormConfigRequested.getFormId(),
            jsonFormConfigRequested.getLanguage()
        );

        domainEventService.subscribe(this);
        domainEventService.receive(formConfigRequested);
    }

    @Override
    public void onEvent(final DomainEvent event) {
        if (event instanceof FormConfigLoaded) {
            FormConfigLoaded formConfigLoaded = (FormConfigLoaded) event;
            JsonFormConfig jsonFormConfig = (JsonFormConfig) FormConfigLoadedParser.getInstance().toExternalEvent(formConfigLoaded);
            FormConfigRequestApiGatewayAdapter.getInstance().onOutputEvent(jsonFormConfig);
        }
    }
}

