
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonOption {

    public List<JsonI18n> text;
    public String value;

}
