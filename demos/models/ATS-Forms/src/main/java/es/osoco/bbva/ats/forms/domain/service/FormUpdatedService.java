package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.events.FormStored;
import es.osoco.bbva.ats.forms.domain.events.FormUpdated;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;

import java.util.function.Consumer;

public class FormUpdatedService implements Consumer<FormUpdated>  {

    final private static FormRepository formRepository = FormRepository.getInstance();

    final Logging logging = LoggingFactory.getInstance().createLogging();

    @Override
    public void accept(FormUpdated domainEvent) {
        Form form = domainEvent.getForm();
        logging.debug("Saving form in language: " + form.getLanguage());
        formRepository.save(form);
        new FormStored(form).emit();
    }
}
