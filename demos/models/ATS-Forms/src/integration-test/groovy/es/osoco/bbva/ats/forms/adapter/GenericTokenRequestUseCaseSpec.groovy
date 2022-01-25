package es.osoco.bbva.ats.forms.adapter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.google.gson.GsonBuilder
import es.osoco.bbva.ats.forms.adapter.json.JsonGenericTokenRequested
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException
import es.osoco.bbva.ats.forms.fixtures.CreateFixtureForm
import es.osoco.bbva.ats.forms.helper.CleanRedisHelper
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class GenericTokenRequestUseCaseSpec extends Specification {

    void 'test application recover request and then draft use case '() {
        given: 'clean redis database'
        CleanRedisHelper.cleanRedis()
        CreateFixtureForm.createForm()

        and: 'a ApiGatewayAdapter instance'

        def formAnsweredApiGatewayAdapter = new FormAnsweredApiGatewayAdapter()
        def genericTokenRequestApiGatewayAdapter = new GenericTokenRequestApiGatewayAdapter()

        and: 'Context with default logger'
        def context = Mock(Context)
        context.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }

        and: 'Submitted finished form sent from old contest'

        JsonFormAnswered formAnswered = getFormAnswered()
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)

        and: 'generic token request'
        Boolean forceNewTokenRequest = false
        Boolean allowRecoverData = true
        String allowRecoverDataText = 'I authorize the use of my previous data to complete this form.'
        JsonGenericTokenRequested jsonGenericTokenRequested = new JsonGenericTokenRequested(
            "a8b6b59d-5e6d-4ac0-8000-b44f8d6a5ca8",
            "ES",
            "generictoken@email.com",
            allowRecoverData,
            allowRecoverDataText,
            forceNewTokenRequest
        )

        when: 'api gateway adapter recieve a message of generic token request type by http'
        def response = genericTokenRequestApiGatewayAdapter.handleRequest(jsonGenericTokenRequested , context)

        then:
        noExceptionThrown()

        JsonSlurper jsonSlurper = new JsonSlurper()
        response != null
        Map responseMap = jsonSlurper.parseText(response)
        responseMap.applicantKey == 'generictoken@email.com'
    }

    @Unroll
    void 'force new token request'() {
        given: 'clean redis database'
        CleanRedisHelper.cleanRedis()
        CreateFixtureForm.createForm()

        and: 'an ApiGatewayAdapter instance'
        def formAnsweredApiGatewayAdapter = new FormAnsweredApiGatewayAdapter()
        def genericTokenRequestApiGatewayAdapter = new GenericTokenRequestApiGatewayAdapter()

        and: 'a context with default logger'
        def context = Mock(Context)
        context.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }

        and: 'a submitted finished form sent from old contest'
        JsonFormAnswered formAnswered = getFormAnswered()
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)

        and: 'the user already has a previously generated token'
        String contestId = "a8b6b59d-5e6d-4ac0-8000-b44f8d6a5ca8"
        String language = "ES"
        String applicantKey = "generictoken@email.com"
        Boolean allowRecoverData = true
        String allowRecoverDataText = 'I authorize the use of my previous data to complete this form.'
        JsonGenericTokenRequested firstJsonGenericTokenRequested = new JsonGenericTokenRequested(
            contestId,
            language,
            applicantKey,
            allowRecoverData,
            allowRecoverDataText,
            true)
        genericTokenRequestApiGatewayAdapter.handleRequest(firstJsonGenericTokenRequested , context)

        when: 'api gateway adapter receives a new token request with the force new token request flag'
        JsonGenericTokenRequested secondJsonGenericTokenRequested = new JsonGenericTokenRequested(
            contestId,
            language,
            applicantKey,
            allowRecoverData,
            allowRecoverDataText,
            givenForceNewTokenRequest)
        def response = genericTokenRequestApiGatewayAdapter.handleRequest(secondJsonGenericTokenRequested , context)

        then: 'a new token will be generated depending on the -forceNewTokenRequest- flag value'
        noExceptionThrown()
        response != null
        Map responseMap = new JsonSlurper().parseText(response)
        responseMap.applicantKey == applicantKey
        responseMap.userHasToken == expectedUserHasTokenResponse

        where:
        givenForceNewTokenRequest | expectedUserHasTokenResponse
        true                      | false
        false                     | true
    }

    @Ignore("When enable this test, all test not run")
    @Unroll
    void 'not generate token when recover from OT17 contest for #contestId contest'() {
        given:
        CleanRedisHelper.cleanRedis()
        CreateFixtureForm.createForm()

        and: 'an ApiGatewayAdapter instance'
        def formAnsweredApiGatewayAdapter = new FormAnsweredApiGatewayAdapter()
        def genericTokenRequestApiGatewayAdapter = new GenericTokenRequestApiGatewayAdapter()

        and: 'a context with default logger'
        def context = Mock(Context)
        context.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }

        and: 'a submitted finished form sent from old contest'
        JsonFormAnswered formAnswered = getFormAnswered()
        formAnswered.contestId = '41c8e0aa-56a2-4ab0-a54b-86e93dee73fe'
        formAnsweredApiGatewayAdapter.handleRequest(formAnswered, context)

        and: 'the user already has a previously generated token'
        String language = "ES"
        String applicantKey = "generictoken@email.com"
        Boolean allowRecoverData = true
        String allowRecoverDataText = 'I authorize the use of my previous data to complete this form.'
        JsonGenericTokenRequested firstJsonGenericTokenRequested = new JsonGenericTokenRequested(
            contestId,
            language,
            applicantKey,
            allowRecoverData,
            allowRecoverDataText,
            false
        )

        when:
        genericTokenRequestApiGatewayAdapter.handleRequest(firstJsonGenericTokenRequested , context)

        then:
        thrown ApplicationNotFoundException

        where:
        contestId << [OT18, OMP]
    }

    private static JsonFormAnswered getFormAnswered() {
        def gson = new GsonBuilder().create()
        def formAnswered = gson.fromJson(formAnsweredJson, JsonFormAnswered.class)
        formAnswered
    }

    private static final String OT18 = "2d19ed29-1d22-0d00-b9df-0ece09ca43f0";
    private static final String OMP = "80cffcea-3f29-0d00-ac17-bbeb0f1d3b77";
    private static final String OT19 = "e2644837-7a42-0d00-98df-937900b12e3b";

    private final static formAnsweredJson = '''{
    "contestId": "a8b6b59d-5e6d-4ac0-8000-b44f8d6a5ca8",
    "formId": "form-1",
    "language": "ES",
    "sections": [
        {
            "sectionId": "5",
            "text": "¿Algo más?",
            "questions": [
                {
                    "id": "38",
                    "required": true,
                    "type": "select",
                    "text": "¿Cómo has conocido Open Talent? (*)",
                    "choices": [
                        "Social",
                        "SouthSummit",
                        "Small&Smart - TPnet",
                        "Bank",
                        "FinTech Connect",
                        "Media",
                        "Payments",
                        "NYPAY",
                        "Pioneers",
                        "Web BBVA Innovation Center",
                        "Finnovista",
                        "Business Rocks",
                        "@dmgerbino",
                        "BBVA",
                        "Let's Talk",
                        "Next",
                        "Web Open Talent",
                        "Seedstars",
                        "Exec I/O Fintech",
                        "Wired Money",
                        "Springwise",
                        "Other"
                    ]
                },
                {
                    "id": "35",
                    "required": false,
                    "type": "longtext",
                    "text": "¿Alguna otra información importante que debamos saber?"
                },
                {
                    "id": "36",
                    "required": false,
                    "type": "link",
                    "text": "¿Algún otro vídeo que quieras compartir con nosotros?"
                },
                {
                    "id": "37",
                    "required": false,
                    "type": "file",
                    "text": "¿Algún otro documento que quieras compartir con nosotros?"
                }
            ]
        },
        {
            "sectionId": "4",
            "text": "Tu producto/servicio",
            "questions": [
                {
                    "id": "32",
                    "required": true,
                    "type": "link",
                    "text": "URL de la web del producto, la app, la demo, etc. (*)"
                },
                {
                    "id": "34",
                    "required": true,
                    "type": "select",
                    "text": "Estado de desarrollo de tu producto (*)",
                    "choices": [
                        "Concept",
                        "Ready to scale",
                        "Prototype",
                        "Commercial version",
                        "Beta version"
                    ]
                },
                {
                    "id": "33",
                    "required": true,
                    "type": "longtext",
                    "text": "Describe tu producto/tecnología. ¿Qué es lo más innovador y disruptivo? (*)"
                }
            ]
        },
        {
            "sectionId": "2",
            "text": "Tus datos personales y tu equipo",
            "questions": [
                {
                    "id": "13",
                    "required": true,
                    "type": "text",
                    "text": "Nombre (*)"
                },
                {
                    "id": "17",
                    "required": true,
                    "type": "select",
                    "text": "Puesto en la empresa (*)",
                    "choices": [
                        "Others",
                        "COO",
                        "CMO",
                        "CIO",
                        "Advisor",
                        "Developer",
                        "CEO",
                        "CTO"
                    ]
                },
                {
                    "id": "20",
                    "required": true,
                    "type": "longtext",
                    "text": "Describe el equipo: rol en la compañía y URL de Linkedin de los componentes más importantes (*)"
                },
                {
                    "id": "16",
                    "required": true,
                    "key": "APPLICANT_KEY",
                    "type": "email",
                    "text": "Email de contacto (*)"
                },
                {
                    "id": "18",
                    "required": false,
                    "type": "link",
                    "text": "LinkedIn"
                },
                {
                    "id": "15",
                    "required": true,
                    "type": "date",
                    "text": "Fecha de nacimiento (*)"
                },
                {
                    "id": "14",
                    "required": true,
                    "type": "text",
                    "text": "Apellidos (*)"
                },
                {
                    "id": "19",
                    "required": false,
                    "type": "number",
                    "text": "Número de teléfono"
                }
            ]
        },
        {
            "sectionId": "3",
            "text": "Tu negocio",
            "questions": [
                {
                    "id": "26",
                    "required": true,
                    "type": "longtext",
                    "text": "¿Cómo haces dinero? Explica tu modelo de negocio (*)"
                },
                {
                    "id": "28",
                    "required": true,
                    "type": "number",
                    "text": "¿Financiación acumulada desde la creación de la empresa? (*)"
                },
                {
                    "id": "21",
                    "required": true,
                    "type": "multiselect",
                    "text": "¿En qué área/sector opera tu startup? (*)",
                    "choices": [
                        "Smart City",
                        "Travel & Tourism",
                        "Entretainment",
                        "Financial Services",
                        "Government",
                        "Other Industries",
                        "Media",
                        "eHealth",
                        "Education",
                        "Cleantech",
                        "eCommerce/eRetail",
                        "eBusiness",
                        "Internet",
                        "Productivity & Work",
                        "Gaming",
                        "Telecoms",
                        "Sports"
                    ]
                },
                {
                    "id": "29",
                    "required": true,
                    "type": "number",
                    "text": "¿Cuáles han sido tus ingresos en 2015? (*)"
                },
                {
                    "id": "30",
                    "required": true,
                    "type": "longtext",
                    "text": "¿Quiénes son tus principales competidores? (*)"
                },
                {
                    "id": "25",
                    "required": true,
                    "type": "select",
                    "text": "Estado de madurez de tu compañía (*)",
                    "choices": [
                        "Mature business",
                        "Growth",
                        "Pre-seed",
                        "Seed",
                        "Early stage"
                    ]
                },
                {
                    "id": "27",
                    "required": true,
                    "type": "longtext",
                    "text": "¿Cuales son tus usuarios objetivo y cómo llegas a ellos? (*)"
                },
                {
                    "id": "23",
                    "required": true,
                    "type": "longtext",
                    "text": "Describe tu negocio en profundidad (*)"
                },
                {
                    "id": "31",
                    "required": false,
                    "type": "longtext",
                    "text": "¿Cuales han sido los mayores logros de la compañía?"
                },
                {
                    "id": "22",
                    "required": true,
                    "type": "longtext",
                    "text": "Tweet: describe tu negocio en 140 caracteres (*)"
                },
                {
                    "id": "24",
                    "required": false,
                    "type": "link",
                    "text": "Elevator pitch: cuéntanos tu proyecto en un vídeo de 1 minuto"
                }
            ]
        },
        {
            "sectionId": "1",
            "text": "La Compañía",
            "questions": [
                {
                    "id": "2",
                    "required": true,
                    "type": "select",
                    "text": "Región de participación (*)",
                    "choices": [
                        "Region III: USA & Rest of the World",
                        "Region I: Europe",
                        "Region II: Latin America"
                    ]
                },
                {
                    "id": "4",
                    "required": true,
                    "type": "link",
                    "text": "Pagina Web de la compañia (*)"
                },
                {
                    "id": "1",
                    "required": true,
                    "key": "APPLICATION_KEY",
                    "type": "text",
                    "text": "Nombre comercial de la compañia (*)"
                },
                {
                    "id": "9",
                    "required": true,
                    "type": "text",
                    "text": "¿En qué país están las oficinas principales de la compañía? (*)"
                },
                {
                    "id": "7",
                    "required": true,
                    "type": "text",
                    "text": "Ciudad de constitución legal (*)"
                },
                {
                    "id": "3",
                    "required": true,
                    "type": "text",
                    "text": "Nombre de la compañía (Razón social) (*)"
                },
                {
                    "id": "5",
                    "required": false,
                    "type": "file",
                    "text": "Logo de la compañia"
                },
                {
                    "id": "11",
                    "required": true,
                    "type": "number",
                    "text": "Número de Empleados (*)"
                },
                {
                    "id": "8",
                    "required": true,
                    "type": "country",
                    "text": "País de constitución legal (*)"
                },
                {
                    "id": "6",
                    "required": false,
                    "type": "text",
                    "text": "ID de Twitter de la compañía"
                },
                {
                    "id": "10",
                    "required": true,
                    "type": "date",
                    "text": "Fecha de constitución legal (*)"
                },
                {
                    "id": "12",
                    "required": true,
                    "type": "number",
                    "text": "Teléfono de contacto (+ código país) (*)"
                }
            ]
        }
    ],
    "answers": [
        {
            "questionId": "20",
            "text": "http://google.com"
        },
        {
            "questionId": "16",
            "text": "generictoken@email.com"
        },
        {
            "questionId": "8",
            "choices": [
                {
                    "id":"Antigua_and_Deps",
                    "label":"Antigua & Deps"
                }
            ]
        },
        {
            "questionId": "30",
            "text": "http://google.com"
        },
        {
            "questionId": "13",
            "text": "http://google.com"
        },
        {
            "questionId": "11",
            "text": "1"
        },
        {
            "questionId": "17",
            "choices": [
                {
                    "id":"cmo",
                    "label":"CMO"
                }
            ]
        },
        {
            "questionId": "33",
            "text": "http://google.com"
        },
        {
            "questionId": "35",
            "text": "volvemos a meter info adicional, pero quitamos el multiselect..."
        },
        {
            "questionId": "15",
            "text": "2017-03-09T09:24:29.375Z"
        },
        {
            "questionId": "23",
            "text": "http://google.com"
        },
        {
            "questionId": "28",
            "text": "1"
        },
        {
            "questionId": "25",
            "choices": [
                {
                    "id":"earlyStage",
                    "label":"Early stage"
                }
            ]
        },
        {
            "questionId": "26",
            "text": "http://google.com"
        },
        {
            "questionId": "2",
            "choices": [
                {
                    "id":"latinAmerica",
                    "label":"Region II: Latin America"
                }
            ]
        },
        {
            "questionId": "7",
            "text": "http://google.com"
        },
        {
            "questionId": "12",
            "text": "1"
        },
        {
            "questionId": "14",
            "text": "http://google.com"
        },
        {
            "questionId": "38",
            "choices": [
                {
                    "id":"nypay",
                    "label":"NYPAY"
                }
            ]
        },
        {
            "questionId": "36",
            "text": "http://google.com"
        },
        {
            "questionId": "10",
            "text": "2017-03-09T09:24:18.868Z"
        },
        {
            "questionId": "29",
            "text": "1"
        },
        {
            "questionId": "27",
            "text": "http://google.com"
        },
        {
            "questionId": "21",
            "choices": [
                {
                    "id":"cleantech",
                    "label": "Cleantech"
                },
                {
                    "id": "ebusiness",
                    "label": "eBusiness"
                },
                {
                    "id":"ehealth",
                    "label": "eHealth"
                }
            ]
        },
        {
            "questionId": "4",
            "text": "http://google.com"
        },
        {
            "questionId": "32",
            "text": "http://google.com"
        },
        {
            "questionId": "1",
            "text": "Este es el nombre de la compañía"
        },
        {
            "questionId": "34",
            "choices": [
                {
                    "id":"prototype",
                    "label":"Prototype"
                }
            ]
        },
        {
            "questionId": "9",
            "text": "http://google.com"
        },
        {
            "questionId": "3",
            "text": "asfdasf"
        },
        {
            "questionId": "22",
            "text": "http://google.com"
        }
    ],
    "origin": "http://localhost:3000/"
}'''


}
