package es.osoco.bbva.ats.forms.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@AllArgsConstructor
@Builder
public class RecoveryPermit implements AggregateRoot {

    private String contestId;

    private String applicantKey;

    private Boolean allowRecoverData;

    private String allowRecoverDataText;

    private ZonedDateTime date;

}
