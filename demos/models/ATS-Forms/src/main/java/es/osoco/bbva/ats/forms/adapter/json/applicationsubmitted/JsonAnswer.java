
package es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonAnswer {

    public String questionId;

    public String text;

    public Set<JsonChoice> choices;

}
