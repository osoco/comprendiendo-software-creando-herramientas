package es.osoco.bbva.ats.forms.domain.util;

public interface Validator<T,S> {

    S executeAndGetFails(T application, boolean strict);

}
