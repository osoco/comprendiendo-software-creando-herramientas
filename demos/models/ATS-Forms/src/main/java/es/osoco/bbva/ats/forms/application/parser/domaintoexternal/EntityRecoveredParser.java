package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonChoice;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonAnswer;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonEntityFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.aggregate.Entity;
import es.osoco.bbva.ats.forms.domain.events.EntityRecovered;
import es.osoco.logging.LoggingFactory;

import java.util.stream.Collectors;

public class EntityRecoveredParser implements Parser<EntityRecovered> {

    public static EntityRecoveredParser getInstance() {
        return EntityRecoveredParser.ApplicationDataRecoveredParserSingletonContainer.SINGLETON;
    }

    @Override
    public ExternalEvent toExternalEvent(EntityRecovered event) {

        Gson gson = new GsonBuilder().create();
        String entityRecoveredJson = gson.toJson(event);
        LoggingFactory.getInstance().createLogging().info(entityRecoveredJson);

        Entity entity = event.getEntity();

        return new JsonEntityFormAnswered(
            event.getApplicantKey(),
            entity.getId(),
            entity.getProperties().getForm().getAnswers().stream().map(
                answer -> new JsonAnswer(
                    answer.getQuestionId(),
                    answer.getText(),
                    answer.getChoices().stream().
                            map(choice -> new JsonChoice(choice.getId(), choice.getLabel())).
                            collect(Collectors.toSet()))
            ).collect(Collectors.toSet())
        );
    }


    protected static final class ApplicationDataRecoveredParserSingletonContainer {
        protected static final EntityRecoveredParser SINGLETON = new EntityRecoveredParser();
    }
}
