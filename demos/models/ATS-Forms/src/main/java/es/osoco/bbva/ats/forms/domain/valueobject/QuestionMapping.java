package es.osoco.bbva.ats.forms.domain.valueobject;

import lombok.Value;

import java.util.Map;

@Value
public class QuestionMapping {

    String id;
    Map<String, String> choices;
}
