package es.osoco.bbva.ats.forms.domain.aggregate;

import com.google.gson.annotations.JsonAdapter;
import es.osoco.bbva.ats.forms.application.util.ChoiceDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@AllArgsConstructor
@Builder
public class Entity implements AggregateRoot {

    private String id;

    private Integer version;

    private String entityType;

    private String applicationId;

    private String externalId;

    private String state;

    private Properties properties;

    @Value
    @AllArgsConstructor
    @Builder
    public static class Properties{
        private Form form;
    }


    @Value
    @AllArgsConstructor
    @Builder
    public static class Form{
        String id;
        List<EntityAnswer> answers;
    }

    @Value
    @AllArgsConstructor
    @Builder
    public static class EntityAnswer {

        private String questionId;
        private String text;
        private Set<Choice> choices;

    }

    @Value
    @AllArgsConstructor
    @Builder
    @JsonAdapter(ChoiceDeserializer.class)
    public static class Choice {

        private String id;
        private String label;

    }
}
