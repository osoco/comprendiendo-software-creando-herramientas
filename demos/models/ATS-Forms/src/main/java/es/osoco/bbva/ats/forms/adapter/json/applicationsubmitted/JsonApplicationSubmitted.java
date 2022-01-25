
package es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonApplicationSubmitted implements ExternalEvent{

    public JsonMeta meta;

    public JsonBody body;

}
