package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.Adapter;
import es.osoco.bbva.ats.forms.adapter.AdapterFactory;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.application.parser.domaintoexternal.ParserFactory;
import es.osoco.bbva.ats.forms.domain.events.DomainEvent;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

import java.util.*;

/**
 * A base class for all {@link UseCaseListener}s.
 * @param <E> the external event.
 */
public abstract class AbstractUseCaseListener<E extends ExternalEvent>
    implements UseCaseListener<E> {

    private String output;

    /**
     * Fallback implementation.
     * @param event the event to process.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final DomainEvent event) {
        final Optional<Parser> eventParser = ParserFactory.getInstance().getParser(event);
        eventParser.ifPresent(parser -> {
            final ExternalEvent externalEvent = parser.toExternalEvent(event);
            final Adapter adapter = AdapterFactory.getAdapter(externalEvent.getClass().getSimpleName());
            if (adapter != null) {
                adapter.onOutputEvent(externalEvent);
            } else {
                getLogging().error("No adapter found for output event " + externalEvent.getClass().getSimpleName());
            }
        });
    }

    /**
     * Retrieves the logging.
     * @return such instance.
     */
    protected Logging getLogging() {
                return LoggingFactory.getInstance().createLogging();
            }

    /**
     * Annotates the output.
     * @param output the output.
     */
    protected void setOutput(final String output) {
        this.output = output;
    }

    /**
     * Retrieves the output.
     * @return such serialized information.
     */
    @Override
    public String getOutput() {
        return this.output;
    }
}
