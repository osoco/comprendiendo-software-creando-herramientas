package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonAnswer;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonChoice;
import es.osoco.bbva.ats.forms.domain.aggregate.Answer;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract  class CommonFormParser {

    private Set<JsonChoice> getChoicesFromAnswer(Answer answer){
        Set<JsonChoice> result = null;
        if (answer.getChoices() !=  null){
            result = answer.getChoices().stream().
                    map(choice -> new JsonChoice(choice.getId(), choice.getLabel())).
                    collect(Collectors.toSet());
        }
        return result;
    }

    protected JsonAnswer createJsonAnswer(Map.Entry<String,Answer> answer){
        return new JsonAnswer(
                answer.getKey(),
                answer.getValue().getText(),
                getChoicesFromAnswer(answer.getValue()));
    }
}
