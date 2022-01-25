package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonCheckbox {

    public List<JsonI18n> helpText;
    public List<JsonI18n> label;
    public List<JsonI18n> errorLabel;
}
