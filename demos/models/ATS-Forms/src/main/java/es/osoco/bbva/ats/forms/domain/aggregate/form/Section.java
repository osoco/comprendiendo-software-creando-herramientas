
package es.osoco.bbva.ats.forms.domain.aggregate.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
@Builder
public class Section {

    private String sectionId;
    private String text;
    private String helpText;
    private List<Question> questions;
    private String type;
    private List<Tab> tabs;

}
