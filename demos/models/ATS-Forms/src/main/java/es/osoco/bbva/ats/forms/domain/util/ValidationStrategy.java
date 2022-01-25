package es.osoco.bbva.ats.forms.domain.util;

import es.osoco.bbva.ats.forms.domain.aggregate.Answer;

public interface ValidationStrategy {
    boolean validate(Answer input);
}
