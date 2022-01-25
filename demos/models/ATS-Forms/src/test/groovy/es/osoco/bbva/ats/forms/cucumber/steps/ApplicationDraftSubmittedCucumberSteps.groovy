package es.osoco.bbva.ats.forms.cucumber.steps

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import es.osoco.bbva.ats.forms.cucumber.support.AutomationApi
import es.osoco.bbva.ats.forms.domain.aggregate.Application
import es.osoco.bbva.ats.forms.domain.events.ApplicationDraftStored
import es.osoco.bbva.ats.forms.domain.events.ApplicationDraftSubmitted
import es.osoco.bbva.ats.forms.domain.events.DomainEvent

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.emptyCollectionOf
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.core.IsNot.not

class ApplicationDraftSubmittedCucumberSteps {

    AutomationApi helper = AutomationApi.instance

    ApplicationDraftSubmitted applicationDraftSubmitted


    @Given('^a partial FormAnswered event with the following form properties:$')
    void given_a_partial_FormAnswered_event_with_the_following_form_properties(DataTable dataTable){

        Map<String, String> applicationMap = dataTable.asMap(String.class, String.class);
        applicationDraftSubmitted = helper.createApplicationDraftSubmitted(
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

        assertThat(applicationDraftSubmitted, is(notNullValue(ApplicationDraftSubmitted.class)))
        assertThat(applicationDraftSubmitted.getApplication().getStatus().name(), equalTo('DRAFT'))
        assertThat(applicationDraftSubmitted.getApplication().getApplicantKey(), is(notNullValue()))
        assertThat(applicationDraftSubmitted.getApplication().getApplicationKey(), is(notNullValue()))
        assertThat(applicationDraftSubmitted.getApplication().getLanguage(), is(notNullValue()))

        helper.setFixture("applicationDraftSubmitted", applicationMap)
    }

    @And('^Store new draft Application$')
    void then_create_new_Application() {
        Map<String,String> applicationMap = helper.getFixture("applicationDraftSubmitted");
        ApplicationDraftSubmitted applicationDraftSubmitted = helper.createApplicationDraftSubmitted(
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


        Application application = helper.applicationStore.findByKey(applicationDraftSubmitted.getApplication().getId())
        assertThat(application, is(notNullValue()))

    }

    @Then('^Validate it except required fiedls and store it$')
    void validate_it_except_required_fiedls_and_store_it() throws Throwable {

        Map<String,String> applicationMap = helper.getFixture("applicationDraftSubmitted");
        ApplicationDraftSubmitted applicationDraftSubmitted = helper.createApplicationDraftSubmitted(
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

        Application application =applicationDraftSubmitted.getApplication()

        assertThat(application.getContestId(),is(notNullValue()))
        assertThat(application.getFormId(),is(notNullValue()))
        assertThat(application.getOrigin(),is(notNullValue()))
        assertThat(application.getApplicantKey(),is(notNullValue()))
        assertThat(application.getApplicationKey(),is(notNullValue()))
        assertThat(application.getLanguage(),is(notNullValue()))
        assertThat(application.getSubmissionDate(),is(notNullValue()))
        assertThat(application.getFirstSubmissionDate(),is(notNullValue()))

        assert (helper.validateApplication(application))

        Application applicationStored = (Application) helper.applicationStore.findByKey(application.getId())
        assertThat(applicationStored, is(notNullValue()))
        assertThat(applicationStored.getAnswersById().get(1), is(equalTo(application.getAnswersById().get(1))))
        assertThat(applicationStored.getRecoveryKey(),is(notNullValue()))

    }

    @And('^FormService publishes (\\d+) ApplicationDraftStored event\\(s\\) with the previous FormAnswered info$')
    void formservice_publishes_ApplicationDraftSubmmit_event_s_with_the_previous_FormAnswered_info(int expectedEvents) throws Throwable {
        List<DomainEvent> events = helper.getBroadcastedEvents();
        assertThat("Forms service doesn't publish any events", events, is(not(emptyCollectionOf(DomainEvent.class))))
        assert (events.stream().filter{event -> event instanceof ApplicationDraftStored}.count() == expectedEvents)
        assertThat(events.stream().filter{event -> event instanceof ApplicationDraftStored}.findFirst().get().getApplication().getRecoveryKey(), is(notNullValue()))
    }



}
