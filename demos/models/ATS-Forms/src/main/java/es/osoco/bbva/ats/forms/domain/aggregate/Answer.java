package es.osoco.bbva.ats.forms.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@AllArgsConstructor
@Getter
@ToString
public class Answer {

    private String text;

    private Set<Choice> choices;

    public boolean isEmpty() {
        return (
            text == null && choices.stream().allMatch(choice -> (choice.getId() == null && choice.getLabel() == null))
        );
    }
}
