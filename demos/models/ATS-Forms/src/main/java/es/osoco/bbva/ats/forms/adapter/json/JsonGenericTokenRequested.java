package es.osoco.bbva.ats.forms.adapter.json;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonGenericTokenRequested  implements ExternalEvent {

    private String contestId;

    private String language;

    private String applicantKey;

    private Boolean allowRecoverData;

    private String allowRecoverDataText;

    private Boolean forceNewToken;
}
