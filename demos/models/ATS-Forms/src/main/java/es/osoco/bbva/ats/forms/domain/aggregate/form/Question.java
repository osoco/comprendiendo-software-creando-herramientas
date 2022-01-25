
package es.osoco.bbva.ats.forms.domain.aggregate.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
@Builder
public class Question {

    private String id;
    private Boolean required;
    private QuestionType type;
    private String subType;
    private String text;
    private String key;
    private Boolean summary;
    private Boolean shareData;
    private String label;
    private String helpText;
    private Integer maxsize;
    private List<String> accept;
    private List<String> preferredCountries;
    private String hint;
    private Responses choices;
    private Boolean showExpanded;
    private Integer maxLength;
    private String regex;
    private Integer cols;
    private Integer maxResponses;
    private String errorMsg;
    private Boolean disabled;
    private String clonesTo;
}
