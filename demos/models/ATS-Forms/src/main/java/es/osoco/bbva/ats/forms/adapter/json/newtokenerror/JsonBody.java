package es.osoco.bbva.ats.forms.adapter.json.newtokenerror;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonBody {

    private String applicantKey;

    private String contestId;

    private String language;

}
