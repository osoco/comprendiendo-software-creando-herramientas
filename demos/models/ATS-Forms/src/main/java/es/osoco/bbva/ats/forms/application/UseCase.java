package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.Adapter;
import es.osoco.bbva.ats.forms.adapter.AdapterFactory;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.ParserFactory;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.bbva.ats.forms.domain.events.DomainEventListener;
import es.osoco.bbva.ats.forms.domain.events.DomainEventService;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingContext;
import es.osoco.logging.LoggingFactory;
import es.osoco.logging.annotations.LoggingPreferences;

import java.util.Optional;

@LoggingPreferences(preferred={"aws-lambda"}, fallback="System.out")
public abstract class UseCase<T> implements DomainEventListener {

    protected final DomainEventService domainEventService = DomainEventService.getInstance();

    abstract public void process(T externalEvent);

    void domainInit(final ExternalEvent incomingEvent) {
        LoggingHelper.getInstance().initLogger(incomingEvent);
        DomainInitializer.getInstance().domainInit();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        final Logging logging = getLogging();
        logging.info("UseCase.onEvent()");

        final Optional<Parser> eventParser = ParserFactory.getInstance().getParser(event);
        eventParser.ifPresent(parser -> {
            ExternalEvent externalEvent = parser.toExternalEvent(event);
            Adapter adapter = AdapterFactory.getAdapter(externalEvent.getClass().getSimpleName());
            if (adapter!= null) {
                adapter.onOutputEvent(externalEvent);
            }
            else {
                logging.info("Not found adapter for output event "
                        + externalEvent.getClass().getSimpleName());
            }
        });
    }

    public Logging getLogging() {
        return LoggingFactory.getInstance().createLogging();
    }

    protected void configureLogging(final Logging logging, final ExternalEvent incomingEvent) {
        final LoggingContext context = logging.getLoggingContext();
        context.put("application", "ats-forms");
        context.put("useCase", this.getClass().getSimpleName());
        context.put("event", incomingEvent.getClass().getSimpleName());
    }
}
