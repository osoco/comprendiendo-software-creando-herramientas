package es.osoco.bbva.ats.forms.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class RecoverException extends RuntimeException {

    private String applicantKey;

    private String contestId;

    private String language;
}
