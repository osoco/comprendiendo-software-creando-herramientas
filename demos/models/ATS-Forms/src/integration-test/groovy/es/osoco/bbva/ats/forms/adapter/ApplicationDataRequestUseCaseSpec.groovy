package es.osoco.bbva.ats.forms.adapter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.google.gson.GsonBuilder
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationDataRecoverRequested
import es.osoco.bbva.ats.forms.adapter.json.JsonGenericTokenRequested
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered
import es.osoco.bbva.ats.forms.adapter.persistence.RedisApplicationStorageAdapter
import es.osoco.bbva.ats.forms.adapter.persistence.RedisGenericTokenStorageAdapter
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException
import es.osoco.bbva.ats.forms.fixtures.CreateFixtureForm
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class ApplicationDataRequestUseCaseSpec extends Specification {

    void 'test application recover request with application in same contest'() {
        given: 'Submitted finished form sent from old contest'
        CreateFixtureForm.createForm()
        JsonFormAnswered formAnswered = getFormAnswered('ot17')
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)

        and: 'Generic token request sent'
        JsonGenericTokenRequested jsonGenericTokenRequested = new JsonGenericTokenRequested(
            contestIdOt17,
            "ES",
            applicantKeyOt17,
            true,
            'I authorize the use of my previous data to complete this form.',
            false
        )
        genericTokenRequestApiGatewayAdapter.handleRequest(jsonGenericTokenRequested, context)

        and: 'recovery key requested by user'
        String applicantKey = getApplicantKeyFromFormAnswered(formAnswered)
        String tokenKey = genericTokenStorageAdapter.findByKey(applicantKey).getToken()

        and: 'application data recover request input message'
        def jsonApplicationDataRecoverRequested = new JsonApplicationDataRecoverRequested(
            contestIdOt17,
            applicantKeyOt17,
            tokenKey)

        when: 'api gateway adapter recieve a message by http'
        def response = applicationDataRecoverRequestApiGatewayAdapter.handleRequest(jsonApplicationDataRecoverRequested, context)

        then:
        noExceptionThrown()
        response

        cleanup: 'clear redis application'
        redisApplicationStorageAdapter.deleteKey("application:$contestIdOt17:$applicantKeyOt17")
    }

    void 'test application recover request with application in OT2018 contest'() {
        given: 'Submitted finished form sent from old contest'
        CreateFixtureForm.createOT18Form()
        JsonFormAnswered formAnswered = getFormAnswered('ot18')
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)

        and: 'Generic token request sent'
        JsonGenericTokenRequested jsonGenericTokenRequested = new JsonGenericTokenRequested(
            contestIdOmp,
            "ES",
            applicantKeyOt18,
            true,
            'I authorize the use of my previous data to complete this form.',
            false)

        when:
        genericTokenRequestApiGatewayAdapter.handleRequest(jsonGenericTokenRequested, context)

        then:
        noExceptionThrown()

        cleanup: 'clear redis application'
        redisApplicationStorageAdapter.deleteKey("application:$contestIdOt18:$applicantKeyOt18")
    }

    void 'application recover request for OT2019 with application in OMP contest'() {
        given: 'Submitted finished form sent from old contest'
        CreateFixtureForm.createOMPForm()
        JsonFormAnswered formAnswered = getFormAnswered('omp')
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)

        and: 'Generic token request sent'
        JsonGenericTokenRequested jsonGenericTokenRequested = new JsonGenericTokenRequested(
            contestIdOt19,
            "ES",
            applicantKeyOMP,
            true,
            'I authorize the use of my previous data to complete this form',
            false)
        genericTokenRequestApiGatewayAdapter.handleRequest(jsonGenericTokenRequested, context)

        and: 'recovery key requested by user'
        RedisGenericTokenStorageAdapter genericTokenStorageAdapter = new RedisGenericTokenStorageAdapter()
        String applicantKey = formAnswered.getAnswers().stream().filter { answer ->
            answer.getQuestionId().equals("omp-0-2")
        }.findFirst().get().getText().toLowerCase()
        String tokenKey = genericTokenStorageAdapter.findByKey(applicantKey).getToken()

        and: 'application data recover request input message'
        JsonApplicationDataRecoverRequested jsonApplicationDataRecoverRequested = new JsonApplicationDataRecoverRequested(
            contestIdOt19,
            applicantKeyOMP,
            tokenKey
        )

        when: 'api gateway adapter receive a message by http'
        String response = applicationDataRecoverRequestApiGatewayAdapter.handleRequest(jsonApplicationDataRecoverRequested, context)

        then:
        noExceptionThrown()
        response

        cleanup: 'clear redis application'
        redisApplicationStorageAdapter.deleteKey("application:$contestIdOt18:$applicantKeyOt18")
    }

    private static JsonFormAnswered getFormAnswered(String contest) {
        def gson = new GsonBuilder().create()
        gson.fromJson(this.getClass().getResource( "/${contest}.form.answered.json" ).text, JsonFormAnswered.class)
    }


    Context context = Mock(Context) {
        it.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }
    }

    @Shared
    FormAnsweredApiGatewayAdapter formAnsweredApiGatewayAdapter = new FormAnsweredApiGatewayAdapter()

    @Shared
    GenericTokenRequestApiGatewayAdapter genericTokenRequestApiGatewayAdapter =
        new GenericTokenRequestApiGatewayAdapter()

    @Shared
    ApplicationDataRecoverRequestApiGatewayAdapter applicationDataRecoverRequestApiGatewayAdapter =
        new ApplicationDataRecoverRequestApiGatewayAdapter()

    @Shared
    RedisGenericTokenStorageAdapter genericTokenStorageAdapter = new RedisGenericTokenStorageAdapter()

    @Shared
    RedisApplicationStorageAdapter redisApplicationStorageAdapter = new RedisApplicationStorageAdapter()

    @Shared
    String contestIdOt17 = '41c8e0aa-56a2-4ab0-a54b-86e93dee73fe'

    @Shared
    String contestIdOt18 = '2d19ed29-1d22-0d00-b9df-0ece09ca43f0'

    @Shared
    String contestIdOmp = '80cffcea-3f29-0d00-ac17-bbeb0f1d3b77'

    @Shared
    String contestIdOt19 = 'e2644837-7a42-0d00-98df-937900b12e3b'

    @Shared
    String applicantKeyOt17 = 'recoverlastapp@ot17.com'

    @Shared
    String applicantKeyOt18 = 'recoverlastapp@ot18.com'

    @Shared
    String applicantKeyOMP = 'recoverlastapp@omp.com'

    private static String getApplicantKeyFromFormAnswered(JsonFormAnswered formAnswered) {
        formAnswered.getAnswers().stream().filter {
            answer -> answer.getQuestionId().equals("16")
        }.findFirst().get().getText().toLowerCase()
    }

    private static void validateResponseAnswers(String contestId, JsonFormAnswered formAnswered, String response) {
        Map contestConverter = new JsonSlurper().parseText(
            new URL(
                "${System.getenv("S3_CONVERT_CONTEST_URL")}/$contestId/${formAnswered.contestId}.json"
            ).text
        )
        List answers = new JsonSlurper().parseText(response).answers

        contestConverter.questions.each { formId, converter ->
            assert formAnswered.answers.find { it.questionId == formId}?.text == answers.find { it.questionId == converter.id}?.text
            if(getAnswerChoices(answers, converter.id)) {
                assert getAnswerChoices(answers, converter.id).containsAll(
                    formAnswered.answers.find { it.questionId == formId}.choices.collect {converter.choices[it]}.findAll()
                )
            }
        }
    }

    private static List getAnswerChoices(List answers, String questionId) {
        answers.find {
            it.questionId == questionId
        }?.choices
    }
}
