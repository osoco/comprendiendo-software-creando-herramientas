package es.osoco.bbva.ats.forms.cucumber.support

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonAnswer
import es.osoco.bbva.ats.forms.application.SubmitApplicationUseCase
import es.osoco.bbva.ats.forms.domain.aggregate.Answer
import es.osoco.bbva.ats.forms.domain.aggregate.Application
import es.osoco.bbva.ats.forms.domain.aggregate.ApplicationStatus
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form
import es.osoco.bbva.ats.forms.domain.aggregate.form.Section
import es.osoco.bbva.ats.forms.domain.config.DomainConfig
import es.osoco.bbva.ats.forms.domain.events.ApplicationDraftSubmitted
import es.osoco.bbva.ats.forms.domain.events.ApplicationSubmitted
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdated
import es.osoco.bbva.ats.forms.domain.events.DomainEvent
import es.osoco.bbva.ats.forms.domain.events.DomainEventListener
import es.osoco.bbva.ats.forms.domain.events.DomainEventService
import es.osoco.bbva.ats.forms.domain.events.FormUpdated
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore
import es.osoco.bbva.ats.forms.domain.util.ApplicationValidator

import java.lang.reflect.Type
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AutomationApi implements DomainEventListener {

    private static final AutomationApi instance = new AutomationApi()

    private AutomationApi() {
        DomainEventService domainEventService = DomainEventService.getNewInstance()
        domainEventService.subscribe(this)
    }

    static AutomationApi getInstance() {
         instance
    }

    @SuppressWarnings("nullness")
    Map getFixture(final String key) {
        return fixture.get(key);
    }


    void setFixture(final String key, Map<String, ?> fixtureData) {
        fixture.put(key, fixtureData);
        addFixture(key, fixtureData);
    }


    void addFixture(final String key, Map<String, ?> fixtureData) {
        fixtureLists.put(key, Collections.singletonList(fixtureData))
    }


    List<Map> getFixtureList(final String key) {
        return fixtureLists.get(key);
    }

    static void sendEventToDomain(final DomainEvent event) {
        DomainEventService domainEventService = DomainEventService.getInstance();
        domainEventService.receive(event);
    }

    FormUpdated createForm(
            String formId,
            String language,
            String sections){
        final Form.FormBuilder builder = new Form.FormBuilder()

        builder
                .formId(formId)
                .language(language)
                .sections(parseSections(sections))

        final FormUpdated formUpdated = new FormUpdated(builder.build())
        pendingEvents.add(formUpdated)
        formUpdated

    }
    ApplicationDraftSubmitted createApplicationDraftSubmitted(
            String id,
            String contestId,
            String applicantId,
            String applicationId,
            String origin,
            String language,
            String status,
            String submissionDate,
            String firstSubmissionDate,
            String formId,
            String answers){

        final Application.ApplicationBuilder builder = new Application.ApplicationBuilder();

        builder
                .formId(formId)
                .contestId(contestId)
                .applicantKey(applicantId)
                .applicationKey(applicationId)
                .origin(origin)
                .language(language)
                .status(ApplicationStatus.valueOf(status.toUpperCase()))
                .answersById(parseAnswers(answers))
                .submissionDate(parseZonedDateTime(submissionDate))
                .firstSubmissionDate(parseZonedDateTime(firstSubmissionDate))

        final ApplicationDraftSubmitted result = new ApplicationDraftSubmitted(builder.build());
        pendingEvents.add(result);
        return result;
    }

    ApplicationSubmitted createApplicationSubmitted(
        String id,
        String contestId,
        String applicantId,
        String applicationId,
        String origin,
        String language,
        String status,
        String submissionDate,
        String firstSubmissionDate,
        String formId,
        String answers){

        final Application.ApplicationBuilder builder = new Application.ApplicationBuilder();

        builder
                .formId(formId)
                .contestId(contestId)
                .applicantKey(applicantId)
                .applicationKey(applicationId)
                .origin(origin)
                .language(language)
                .status(ApplicationStatus.valueOf(status.toUpperCase()))
                .answersById(parseAnswers(answers))
                .submissionDate(parseZonedDateTime(submissionDate))
                .firstSubmissionDate(parseZonedDateTime(firstSubmissionDate))

        final ApplicationSubmitted result = new ApplicationSubmitted(builder.build());
        pendingEvents.add(result);
        return result;
    }

    ApplicationUpdated createApplicationUpdated(
            String id,
            String contestId,
            String applicantId,
            String applicationId,
            String origin,
            String language,
            String status,
            String submissionDate,
            String firstSubmissionDate,
            String formId,
            String answers){

        final Application.ApplicationBuilder builder = new Application.ApplicationBuilder();

        builder
                .formId(formId)
                .contestId(contestId)
                .applicantKey(applicantId)
                .applicationKey(applicationId)
                .origin(origin)
                .language(language)
                .status(ApplicationStatus.valueOf(status.toUpperCase()))
                .answersById(parseAnswers(answers))
                .submissionDate(parseZonedDateTime(submissionDate))
                .firstSubmissionDate(parseZonedDateTime(firstSubmissionDate))

        final ApplicationUpdated result = new ApplicationUpdated(builder.build());
        pendingEvents.add(result);
        return result;
    }

    List<DomainEvent> getPendingEvents() {
        pendingEvents
    }

    List<DomainEvent> getBroadcastedEvents() {
         broadcastedEvents;
    }

    Boolean validateApplication(Application application){
        applicationValidator.executeAndGetFails(application, true).isEmpty();
    }

    Boolean validateApplicationRequiredFields(Application application){
        applicationValidator.validateRequiredFields(application).isEmpty();
    }

    @Override
    void onEvent(final DomainEvent event) {
        broadcastedEvents.add(event);
    }

    private SubmitApplicationUseCase createSubmitApplicationUseCase (){
        new DomainConfig.Builder()
                .applicationAggregateStoreAdapter(this.applicationStore)
                .tokenAggregateStoreAdapter(this.recoveryTokenStore)
                .formAggregateStoreAdapter(this.formStore)
                .build();
        return SubmitApplicationUseCase.getInstance();
    }

    private Map<String, Answer> parseAnswers(String answers){
        Map<String, Answer> result = new HashMap<>()

        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<Set<JsonAnswer>>(){}.getType()
        final Set<JsonAnswer> answersSet = gson.fromJson(answers, listType)

        answersSet.forEach({
            answer -> result.put(answer.getQuestionId(),new Answer(answer.getText(), answer.getChoices()))
        })
        result
    }

    private List<Section> parseSections(String sections){
        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<List<Section>>(){}.getType()
        return gson.fromJson(sections, listType);
    }

    private ZonedDateTime parseZonedDateTime(String date){
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return localDate.atStartOfDay(ZoneId.systemDefault());
    }

    private AggregateStore applicationStore = new ApplicationMemoryRepository()
    private AggregateStore recoveryTokenStore = new RecoveryTokenMemoryRepository()
    private AggregateStore formStore = new FormMemoryRepository()
    private SubmitApplicationUseCase submitApplicationUseCase = createSubmitApplicationUseCase()
    private Map<String, Map> fixture = new HashMap<>()
    private Map<String, List<Map>> fixtureLists = new HashMap<>()
    private List<DomainEvent> pendingEvents = new ArrayList<>()
    private List<DomainEvent> broadcastedEvents = new ArrayList<>()
    private ApplicationValidator applicationValidator = ApplicationValidator.getInstance()

}
