
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonLegalConditions {

    public List<JsonI18n> link;
    public List<JsonI18n> text = null;
    public List<JsonI18n> linkText = null;

}
