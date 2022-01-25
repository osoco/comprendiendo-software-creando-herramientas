package es.osoco.bbva.ats.forms.domain.exception;

public class MappingContestErrorException extends RuntimeException {

    public MappingContestErrorException (String message, Throwable cause) {
        super(message, cause);
    }
}