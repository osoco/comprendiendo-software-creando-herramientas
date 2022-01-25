package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.domain.events.DomainEventListener;

/**
 * Listener to translate domain events into external events.
 * It's not thread-safe. Only one instance should be used for each request.
 */
public interface UseCaseListener<E extends ExternalEvent> extends DomainEventListener {

    /**
     * Processes given output event.
     * @param payload the event.
     */
    void onOutputEvent(E payload);

    /**
     * Retrieves the output.
     * @return the serialized version from the last output event.
     */
    String getOutput();
}
