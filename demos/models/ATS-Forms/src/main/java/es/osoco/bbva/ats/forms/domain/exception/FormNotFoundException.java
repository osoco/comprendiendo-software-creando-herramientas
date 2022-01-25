package es.osoco.bbva.ats.forms.domain.exception;

public class FormNotFoundException
    extends RuntimeException {
    public FormNotFoundException(final String formId) {
        super("No form with id " + formId);
    }
}
