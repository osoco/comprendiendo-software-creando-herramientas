package es.osoco.bbva.ats.forms.domain.events;

import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class FormStored implements DomainEvent{

    private Form form;

}
