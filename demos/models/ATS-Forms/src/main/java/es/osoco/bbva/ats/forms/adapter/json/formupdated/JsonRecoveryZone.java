
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonRecoveryZone {

    public List<JsonI18n> button;
    public List<JsonI18n> message;
    public List<JsonI18n> title;
    public JsonCheckbox checkbox;

}
