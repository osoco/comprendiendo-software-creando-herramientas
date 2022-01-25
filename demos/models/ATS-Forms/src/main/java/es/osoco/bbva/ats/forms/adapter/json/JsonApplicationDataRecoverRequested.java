package es.osoco.bbva.ats.forms.adapter.json;

import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonApplicationDataRecoverRequested implements ExternalEvent {

    private String contestId;

    private String applicantKey;

    private String applicantRecoveryKey;

}
