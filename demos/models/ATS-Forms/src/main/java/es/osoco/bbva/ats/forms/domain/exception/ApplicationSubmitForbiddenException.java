package es.osoco.bbva.ats.forms.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ApplicationSubmitForbiddenException extends RuntimeException {

}
