
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonSection {

    public String id;
    public String type;
    public List<JsonI18n> text = null;
    public List<JsonQuestion> questions = null;
    public List<JsonI18n> helpText = null;
    public List<JsonTab> tabs = null;

}
