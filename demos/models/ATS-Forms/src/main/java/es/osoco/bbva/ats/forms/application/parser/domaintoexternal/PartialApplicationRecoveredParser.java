package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.PartialApplicationRecovered;
import es.osoco.logging.LoggingFactory;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class PartialApplicationRecoveredParser extends CommonFormParser implements Parser<PartialApplicationRecovered> {

    public static PartialApplicationRecoveredParser getInstance() {
        return PartialApplicationRecoveredParser.PartialApplicationRecoveredParserSingletonContainer.SINGLETON;
    }

    @Override
    public ExternalEvent toExternalEvent(PartialApplicationRecovered event) {

        Gson gson = new GsonBuilder().create();
        String applicationSubmittedJson = gson.toJson(event);
        LoggingFactory.getInstance().createLogging().info(applicationSubmittedJson);

        Application application = event.getApplication();

        return new JsonFormAnswered(
                        application.getContestId(),
                        application.getFormId(),
                        application.getLanguage().toLowerCase(),
                        application.getStatus().name(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(application.getSubmissionDate()),
                        application.getEntityId(),
                        application.getAnswersById().entrySet().stream().
                                map(this::createJsonAnswer).
                                collect(Collectors.toSet()),
                        application.getOrigin()
        );
    }


    protected static final class PartialApplicationRecoveredParserSingletonContainer {
        protected static final PartialApplicationRecoveredParser SINGLETON = new PartialApplicationRecoveredParser();
    }


}
