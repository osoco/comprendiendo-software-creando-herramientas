
package es.osoco.bbva.ats.forms.adapter.json.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonMeta {

    public String id;

    public Integer version;

    public String timestamp;

    public String type;

}
