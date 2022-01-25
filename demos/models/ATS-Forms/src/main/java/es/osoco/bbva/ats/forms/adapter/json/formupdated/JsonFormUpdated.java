
package es.osoco.bbva.ats.forms.adapter.json.formupdated;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonFormUpdated implements ExternalEvent {

    public JsonMeta meta;
    public JsonBody body;

}
