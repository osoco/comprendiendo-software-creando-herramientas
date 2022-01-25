package es.osoco.bbva.ats.forms.cucumber.steps

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import es.osoco.bbva.ats.forms.cucumber.support.AutomationApi
import es.osoco.bbva.ats.forms.domain.aggregate.Application
import es.osoco.bbva.ats.forms.domain.events.ApplicationStored
import es.osoco.bbva.ats.forms.domain.events.ApplicationSubmitted
import es.osoco.bbva.ats.forms.domain.events.DomainEvent

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.emptyCollectionOf
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.isIn
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.core.IsNot.not

class ApplicationSubmittedCucumberSteps {

    AutomationApi helper = AutomationApi.instance

    ApplicationSubmitted applicationSubmitted

    @Given('^a complete FormAnswered event with the following form properties:$')
    void given_a_complete_formanswered_event_with_the_following_form_properties(DataTable dataTable){

        Map<String, String> applicationMap = dataTable.asMap(String.class, String.class);
        applicationSubmitted = helper.createApplicationSubmitted(
                applicationMap.get("id"),
                applicationMap.get("contestId"),
                applicationMap.get("applicantId"),
                applicationMap.get("applicationId"),
                applicationMap.get("origin"),
                applicationMap.get("language"),
                applicationMap.get("status"),
                applicationMap.get("submissionDate"),
                applicationMap.get("firstSubmissionDate"),
                applicationMap.get("formId"),
                applicationMap.get("answers"))

        assertThat(applicationSubmitted, is(notNullValue(ApplicationSubmitted.class)))

        assertThat(applicationSubmitted, is(notNullValue(ApplicationSubmitted.class)))
        assertThat(applicationSubmitted.getApplication().getStatus().name(), isIn(['DRAFT', 'FINISHED']))
        assertThat(applicationSubmitted.getApplication().getApplicantKey(), is(notNullValue()))
        assertThat(applicationSubmitted.getApplication().getApplicationKey(), is(notNullValue()))
        assertThat(applicationSubmitted.getApplication().getLanguage(), is(notNullValue()))

        helper.setFixture("applicationSubmitted", applicationMap)
    }

    @When('^FormService receives the previous event$')
    void when_formService_receives_the_previous_event() {
        List<DomainEvent> events = helper.getPendingEvents();
        assertThat("There must be at least one event available to be consumed",
                events,
                is(not(emptyCollectionOf(DomainEvent.class))));

        events.forEach({event -> helper.sendEventToDomain(event)});
    }

    @And('^Store new complete Application$')
    void then_create_new_Application() {
        Map<String,String> applicationMap = helper.getFixture("applicationSubmitted");
        ApplicationSubmitted applicationSubmitted = helper.createApplicationSubmitted(
                applicationMap.get("id"),
                applicationMap.get("contestId"),
                applicationMap.get("applicantId"),
                applicationMap.get("applicationId"),
                applicationMap.get("origin"),
                applicationMap.get("language"),
                applicationMap.get("status"),
                applicationMap.get("submissionDate"),
                applicationMap.get("firstSubmissionDate"),
                applicationMap.get("formId"),
                applicationMap.get("answers"))

        helper.applicationStore.save(applicationSubmitted.getApplication())
        assert helper.applicationStore.findByKey(applicationSubmitted.getApplication().getId()) != null
    }

    @Then('^Validate it and check that it\'s complete$')
    void and_validate_it_and_check_that_its_complete(){

        Map<String, String> applicationMap = helper.getFixture("applicationSubmitted")

        Application application = helper.createApplicationSubmitted(
                applicationMap.get("id"),
                applicationMap.get("contestId"),
                applicationMap.get("applicantId"),
                applicationMap.get("applicationId"),
                applicationMap.get("origin"),
                applicationMap.get("language"),
                applicationMap.get("status"),
                applicationMap.get("submissionDate"),
                applicationMap.get("firstSubmissionDate"),
                applicationMap.get("formId"),
                applicationMap.get("answers")).getApplication()


        assertThat(application.getContestId(),is(notNullValue()))
        assertThat(application.getFormId(),is(notNullValue()))
        assertThat(application.getOrigin(),is(notNullValue()))
        assertThat(application.getApplicantKey(),is(notNullValue()))
        assertThat(application.getApplicationKey(),is(notNullValue()))
        assertThat(application.getLanguage(),is(notNullValue()))
        assertThat(application.getSubmissionDate(),is(notNullValue()))
        assertThat(application.getFirstSubmissionDate(),is(notNullValue()))

        assert (helper.validateApplication(application))
        assert (helper.validateApplicationRequiredFields(application))

        Application applicationStored = (Application) helper.applicationStore.findByKey(application.getId())
        assertThat(applicationStored, is(notNullValue()))
        assertThat(applicationStored.getAnswersById().get(1), is(equalTo(application.getAnswersById().get(1))))
    }


    @And('^FormService publishes (\\d+) ApplicationStored event\\(s\\) with the previous FormAnswered info$')
    void formservice_publishes_ApplicationStored_event_s_with_the_previous_FormAnswered_info(int expectedEvents) throws Throwable {
        List<DomainEvent> events = helper.getBroadcastedEvents();
        assertThat("Forms service doesn't publish any events", events, is(not(emptyCollectionOf(DomainEvent.class))))
        assert (events.stream().filter{event -> event instanceof ApplicationStored}.count() == expectedEvents)
        assertThat(events.stream().filter{event -> event instanceof ApplicationStored}.findFirst().get().getApplication().getRecoveryKey(), is(notNullValue()))
    }


}
