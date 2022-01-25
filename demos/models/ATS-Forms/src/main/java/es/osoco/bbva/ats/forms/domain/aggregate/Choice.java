package es.osoco.bbva.ats.forms.domain.aggregate;

import com.google.gson.annotations.JsonAdapter;
import es.osoco.bbva.ats.forms.application.util.ChoiceDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@JsonAdapter(ChoiceDeserializer.class)
@ToString
public class Choice {

    private String id;

    private String label;
}
