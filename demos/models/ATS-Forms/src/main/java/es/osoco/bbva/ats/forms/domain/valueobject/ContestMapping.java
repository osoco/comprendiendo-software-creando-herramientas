package es.osoco.bbva.ats.forms.domain.valueobject;

import lombok.Value;

import java.util.Map;

@Value
public class ContestMapping {

    Map<String, QuestionMapping> questions;

}
