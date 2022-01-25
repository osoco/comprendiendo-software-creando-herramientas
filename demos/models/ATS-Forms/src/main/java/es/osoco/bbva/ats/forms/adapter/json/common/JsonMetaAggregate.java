
package es.osoco.bbva.ats.forms.adapter.json.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class JsonMetaAggregate extends JsonMeta{

    public String aggregate;

    public JsonMetaAggregate(String id, Integer version, String timestamp, String type, String aggregate) {
        this.id = id;
        this.version = version;
        this.timestamp = timestamp;
        this.type = type;
        this.aggregate = aggregate;
    }
}
