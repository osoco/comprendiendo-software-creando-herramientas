package es.osoco.bbva.ats.forms.adapter.json.emailtoken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonBody {

    private String language;

    private String contestId;
    
    private String applicantKey;

    private String emailToken;

}
