
package es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class JsonQuestion {

    public String id;

    public boolean required;

    public String type;

    public String text;

    public Set<String> choices = null;
}
