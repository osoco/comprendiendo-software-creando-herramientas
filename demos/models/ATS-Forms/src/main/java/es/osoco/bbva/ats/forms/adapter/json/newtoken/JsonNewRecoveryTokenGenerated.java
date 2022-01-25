package es.osoco.bbva.ats.forms.adapter.json.newtoken;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonNewRecoveryTokenGenerated implements ExternalEvent {

    public JsonMeta meta;

    public JsonBody body;
}
