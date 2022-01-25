package es.osoco.bbva.ats.forms.domain.service

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.google.gson.GsonBuilder
import es.osoco.bbva.ats.forms.adapter.FormAnsweredApiGatewayAdapter
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered
import es.osoco.bbva.ats.forms.adapter.persistence.RedisApplicationStorageAdapter
import es.osoco.bbva.ats.forms.domain.events.GenericTokenRequested
import es.osoco.bbva.ats.forms.domain.exception.ApplicationToRecoverNotFoundException
import es.osoco.bbva.ats.forms.domain.exception.DataRecoveryNotAllowedException
import es.osoco.bbva.ats.forms.fixtures.CreateFixtureForm
import spock.lang.Shared
import spock.lang.Specification

class GenericTokenRequestedServiceSpec extends Specification {

    void 'User tries to recover an application without allowing to recover it'() {
        given:
        CreateFixtureForm.createOT19Form()
        JsonFormAnswered formAnswered = getFormAnswered('ot19')
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)
        genericTokenRequested = new GenericTokenRequested(
            contestId,
            language,
            formAnswered.answers.find { it.questionId == 'ot19-0-2' }.text,
            false,
            allowRecoverDataText,
            forceNewToken
        )

        when:
        genericTokenRequestedService.accept(genericTokenRequested)

        then:
        thrown(DataRecoveryNotAllowedException)

        cleanup: 'clear redis application'
        redisApplicationStorageAdapter.deleteKey("application:$contestId19:$genericTokenRequested.applicantKey")
    }

    void 'User tries to recover an application that does not exist'() {
        given:
        genericTokenRequested = new GenericTokenRequested(
            contestId,
            language,
            'Applicant-key',
            true,
            allowRecoverDataText,
            forceNewToken
        )

        when:
        genericTokenRequestedService.accept(genericTokenRequested)

        then:
        thrown(ApplicationToRecoverNotFoundException)
    }

    void 'Recover an application with permissions to recover'() {
        given:
        CreateFixtureForm.createOT19Form()
        JsonFormAnswered formAnswered = getFormAnswered('ot19')
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)
        genericTokenRequested = new GenericTokenRequested(
            contestId,
            language,
            formAnswered.answers.find { it.questionId == 'ot19-0-2' }.text,
            true,
            allowRecoverDataText,
            forceNewToken
        )

        when:
        genericTokenRequestedService.accept(genericTokenRequested)

        then:
        noExceptionThrown()
    }

    void 'Recover an application from the same contest '() {
        given:
        JsonFormAnswered formAnswered = getFormAnswered('ot18')
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)
        genericTokenRequested = new GenericTokenRequested(
            contestId18,
            language,
            formAnswered.answers.find { it.questionId == 'ot18-0-2' }.text,
            true,
            allowRecoverDataText,
            forceNewToken
        )

        when:
        genericTokenRequestedService.accept(genericTokenRequested)

        then:
        noExceptionThrown()
    }

    GenericTokenRequested genericTokenRequested

    def setup() {
        CreateFixtureForm.createOT18Form()
    }
    def cleanup() {
        redisApplicationStorageAdapter.deleteKey("application:$contestId18:$genericTokenRequested.applicantKey")
    }

    Context context = Mock(Context) {
        it.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }
    }

    private static JsonFormAnswered getFormAnswered(String contest) {
        def gson = new GsonBuilder().create()
        gson.fromJson(this.getClass().getResource("/${contest}.form.answered.json").text, JsonFormAnswered.class)
    }

    @Shared
    RedisApplicationStorageAdapter redisApplicationStorageAdapter = new RedisApplicationStorageAdapter()

    @Shared
    FormAnsweredApiGatewayAdapter formAnsweredApiGatewayAdapter = new FormAnsweredApiGatewayAdapter()

    @Shared
    String contestId = '80cffcea-3f29-0d00-ac17-bbeb0f1d3b77'

    @Shared
    String contestId18 = '2d19ed29-1d22-0d00-b9df-0ece09ca43f0'

    @Shared
    String contestId19 = 'e2644837-7a42-0d00-98df-937900b12e3b'

    @Shared
    String language = 'ES'

    @Shared
    String allowRecoverDataText = 'text'

    @Shared
    Boolean forceNewToken = false

    @Shared
    GenericTokenRequestedService genericTokenRequestedService = new GenericTokenRequestedService()

}
