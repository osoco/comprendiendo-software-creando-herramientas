package es.osoco.bbva.ats.forms.application.parser;


import es.osoco.bbva.ats.forms.adapter.ExternalEvent;

public interface Parser<T> {

    ExternalEvent toExternalEvent(T domainEvent);
}
