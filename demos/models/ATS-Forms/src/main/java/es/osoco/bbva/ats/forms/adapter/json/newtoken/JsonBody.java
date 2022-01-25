package es.osoco.bbva.ats.forms.adapter.json.newtoken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonBody {

    private String contestId;

    private String language;
    
    private String applicantKey;

    private String recoveryKey;
}
