package es.osoco.bbva.ats.forms.cucumber.steps

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import es.osoco.bbva.ats.forms.cucumber.support.AutomationApi
import es.osoco.bbva.ats.forms.domain.aggregate.Application
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdateStored
import es.osoco.bbva.ats.forms.domain.events.ApplicationUpdated
import es.osoco.bbva.ats.forms.domain.events.DomainEvent
import org.hamcrest.MatcherAssert

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.emptyCollectionOf
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.isIn
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.core.IsNot.not

class ApplicationUpdatedCucumberSteps {

    AutomationApi helper = AutomationApi.instance

    ApplicationUpdated applicationUpdated

    @Given('^a FormAnswered event with the following form properties:$')
    void given_a_formanswered_event_with_the_following_form_properties(DataTable dataTable){

        Map<String, String> applicationMap = dataTable.asMap(String.class, String.class);
        applicationUpdated = helper.createApplicationUpdated(
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

        assertThat(applicationUpdated, is(notNullValue(ApplicationUpdated.class)))

        assertThat(applicationUpdated, is(notNullValue(ApplicationUpdated.class)))
        assertThat(applicationUpdated.getApplication().getStatus().name(), isIn(['DRAFT', 'FINISHED']))
        MatcherAssert.assertThat(applicationUpdated.getApplication().getApplicantKey(), is(notNullValue()))
        MatcherAssert.assertThat(applicationUpdated.getApplication().getApplicationKey(), is(notNullValue()))
        MatcherAssert.assertThat(applicationUpdated.getApplication().getLanguage(), is(notNullValue()))

        helper.setFixture("applicationUpdated", applicationMap)
    }


    @And('^Store update Application$')
    void then_create_new_Application() {
        Map<String,String> applicationMap = helper.getFixture("applicationUpdated");
        ApplicationUpdated applicationUpdated = helper.createApplicationUpdated(
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

        helper.applicationStore.save(applicationUpdated.getApplication())
        assert helper.applicationStore.findByKey(applicationUpdated.getApplication().getId()) != null
    }

    @Then('^Validate it$')
    void and_validate_it_and_check_that_its_complete(){

        Map<String, String> applicationMap = helper.getFixture("applicationUpdated")

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


        MatcherAssert.assertThat(application.getContestId(),is(notNullValue()))
        MatcherAssert.assertThat(application.getFormId(),is(notNullValue()))
        MatcherAssert.assertThat(application.getOrigin(),is(notNullValue()))
        MatcherAssert.assertThat(application.getApplicantKey(),is(notNullValue()))
        MatcherAssert.assertThat(application.getApplicationKey(),is(notNullValue()))
        MatcherAssert.assertThat(application.getLanguage(),is(notNullValue()))
        assertThat(application.getSubmissionDate(),is(notNullValue()))
        assertThat(application.getFirstSubmissionDate(),is(notNullValue()))

        assert (helper.validateApplication(application))

        Application applicationUpdateStored = (Application) helper.applicationStore.findByKey(application.getId())
        assertThat(applicationUpdateStored, is(notNullValue()))
        assertThat(applicationUpdateStored.getAnswersById().get(1), is(equalTo(application.getAnswersById().get(1))))
    }


    @And('^FormService publishes (\\d+) ApplicationUpdateStored event\\(s\\) with the previous FormAnswered info$')
    void formservice_publishes_ApplicationUpdateStored_event_s_with_the_previous_FormAnswered_info(int expectedEvents) throws Throwable {
        List<DomainEvent> events = helper.getBroadcastedEvents();
        assertThat("Forms service doesn't publish any events", events, is(not(emptyCollectionOf(DomainEvent.class))))
        assert (events.stream().filter{event -> event instanceof ApplicationUpdateStored}.count() == expectedEvents)
        assertThat(events.stream().filter{event -> event instanceof ApplicationUpdateStored}.findFirst().get().getApplication().getRecoveryKey(), is(notNullValue()))
    }


}
