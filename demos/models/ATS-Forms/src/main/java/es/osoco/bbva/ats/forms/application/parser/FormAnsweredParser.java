package es.osoco.bbva.ats.forms.application.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonAnswer;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonApplicationSubmitted;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonChoice;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeSerializer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.ApplicationSubmitted;
import es.osoco.logging.LoggingFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

public class FormAnsweredParser implements Parser<ApplicationSubmitted> {

    public static FormAnsweredParser getInstance() {
        return FormAnsweredParser.FormAnsweredParserSingletonContainer.SINGLETON;
    }

    protected static final class FormAnsweredParserSingletonContainer {
        protected static final FormAnsweredParser SINGLETON = new FormAnsweredParser();
    }

    @Override
    public ExternalEvent toExternalEvent(ApplicationSubmitted event) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer()).create();
        String applicationSubmittedJson = gson.toJson(event);
        LoggingFactory.getInstance().createLogging().info(applicationSubmittedJson);

        Application application = event.getApplication();

        return new JsonApplicationSubmitted(
                new JsonMeta(
                        UUID.randomUUID().toString(),
                        2,
                        LocalDateTime.now().toString(),
                        "FORM_SUBMITTED"),
                new JsonBody(
                        application.getVersion(),
                        application.getFormId(),
                        application.getContestId(),
                        application.getApplicantKey(),
                        application.getApplicationKey(),
                        application.getRecoveryKey(),
                        application.getStatus().name(),
                        application.getLanguage(),
                        application.getAnswersById().entrySet().stream().
                                map(entry -> new JsonAnswer(
                                        entry.getKey(),
                                        entry.getValue().getText(),
                                        entry.getValue().getChoices().stream().
                                                map(choice -> new JsonChoice(choice.getId(), choice.getLabel())).
                                                collect(Collectors.toSet()))).
                                collect(Collectors.toSet()),
                        application.getOrigin(),
                        dateTimeFormatter.format(LocalDateTime.now()),
                        dateTimeFormatter.format(application.getFirstSubmissionDate()),
                        application.getEntityId()
                ));
    }
}
