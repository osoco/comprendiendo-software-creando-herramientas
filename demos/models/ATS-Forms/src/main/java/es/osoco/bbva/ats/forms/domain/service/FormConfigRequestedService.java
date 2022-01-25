package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.events.FormConfigLoaded;
import es.osoco.bbva.ats.forms.domain.events.FormConfigRequested;
import es.osoco.bbva.ats.forms.domain.exception.FormNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.FormRepository;

import java.util.function.Consumer;

public class FormConfigRequestedService implements Consumer<FormConfigRequested> {

    private static final FormRepository formRepository = FormRepository.getInstance();

    @Override
    public void accept(FormConfigRequested formConfigRequested) {
        String formKey = formConfigRequested.getFormId() + ":" + formConfigRequested.getLanguage();
        Form form = formRepository.byID(formKey);

        if (form != null) {
            new FormConfigLoaded(form).emit();
        } else {
            throw new FormNotFoundException(formKey);
        }
    }
}
