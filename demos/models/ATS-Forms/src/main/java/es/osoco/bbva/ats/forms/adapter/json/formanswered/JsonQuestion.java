
package es.osoco.bbva.ats.forms.adapter.json.formanswered;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonQuestion {

    public String id;

    public boolean required;

    public String key;

    public String type;

    public String text;

    public Set<String> choices = null;

}
