package es.osoco.bbva.ats.forms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class GenericTokenRequested  implements DomainEvent{
    private String contestId;

    private String language;

    private String applicantKey;

    private Boolean allowRecoverData;

    private String allowRecoverDataText;

    private Boolean forceNewToken;
}