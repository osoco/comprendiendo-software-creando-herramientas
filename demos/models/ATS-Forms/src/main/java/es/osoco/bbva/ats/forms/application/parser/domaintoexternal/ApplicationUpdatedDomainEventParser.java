package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonApplicationSubmitted;
import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonBody;
import es.osoco.bbva.ats.forms.adapter.json.common.JsonMeta;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeSerializer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.events.ApplicationDomainEvent;
import es.osoco.logging.LoggingFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

public class ApplicationUpdatedDomainEventParser extends CommonFormParser implements Parser<ApplicationDomainEvent> {

    public static ApplicationUpdatedDomainEventParser getInstance() {
        return ApplicationDomainEventParserSingletonContainer.SINGLETON;
    }

    protected static final class ApplicationDomainEventParserSingletonContainer {
        protected static final ApplicationUpdatedDomainEventParser SINGLETON = new ApplicationUpdatedDomainEventParser();
    }

    final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public ExternalEvent toExternalEvent(ApplicationDomainEvent event) {

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
                        "UPDATE_APPLICATION"),
                new JsonBody(
                        application.getVersion(),
                        application.getFormId(),
                        application.getContestId(),
                        application.getApplicantKey(),
                        application.getApplicationKey(),
                        application.getRecoveryKey(),
                        application.getStatus().toString(),
                        application.getLanguage(),
                        application.getAnswersById().entrySet().stream().
                                map(this::createJsonAnswer).
                                collect(Collectors.toSet()),
                        application.getOrigin(),
                        DATE_TIME_FORMATTER.format(application.getSubmissionDate()),
                        DATE_TIME_FORMATTER.format(application.getFirstSubmissionDate()),
                        application.getEntityId()));
    }



}
