package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.FormConfigRequestApiGatewayAdapter;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonFormConfigRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormConfig;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.FormConfigLoadedParser;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.DomainEventService;
import es.osoco.bbva.ats.forms.domain.events.FormConfigLoaded;
import es.osoco.bbva.ats.forms.domain.events.FormConfigRequested;
import es.osoco.bbva.ats.forms.domain.events.DomainEventListener;

public class GetFormUseCase {

    public static GetFormUseCase getInstance() {
        return GetFormUseCaseSingletonContainer.SINGLETON;
    }

    protected static final class GetFormUseCaseSingletonContainer {
        protected static final GetFormUseCase SINGLETON = new GetFormUseCase();
    }

    public void process(JsonFormConfigRequested jsonFormConfigRequested, final GetFormListener listener) {
        DomainInitializer.getInstance().domainInit();
        LoggingHelper.getInstance().initLogger(jsonFormConfigRequested);
        final FormConfigRequested formConfigRequested =
            new FormConfigRequested(
                                    jsonFormConfigRequested.getFormId(),
                                    jsonFormConfigRequested.getLanguage());

        final DomainEventService service = DomainEventService.getInstance();
        service.subscribe(event -> {
            if (event instanceof FormConfigLoaded) {
                FormConfigLoaded formConfigLoaded = (FormConfigLoaded) event;
                JsonFormConfig jsonFormConfig = (JsonFormConfig) FormConfigLoadedParser.getInstance().toExternalEvent(formConfigLoaded);
                listener.onOutputEvent(jsonFormConfig);
            }
        });
        service.receive(formConfigRequested);
    }
}

