package es.osoco.bbva.ats.forms.adapter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.osoco.bbva.ats.forms.adapter.json.JsonFormConfigRequested
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated
import es.osoco.bbva.ats.forms.domain.exception.FormNotFoundException
import spock.lang.Specification
import java.lang.reflect.Type

class FormConfigRequestApiGatewayAdapterSpec extends Specification {

    void 'form config request with an invalid formId throws an Exception'() {
        given: 'a ApiGatewayAdapter instance'
        def apiGatewayAdapter = new FormConfigRequestApiGatewayAdapter()

        and: 'a valid stored form'
        Type type = new TypeToken<JsonFormUpdated>() {}.getType()
        JsonFormUpdated jsonFormUpdated = new Gson().fromJson(formUpdatedJson, type)
        new FormUpdatedRabbitMqAdapter().onInputEvent(jsonFormUpdated)

        and: 'a invalid form config request input message'
        def entityRecoverRequest = new JsonFormConfigRequested ('__invalid_form_id__', 'lang_es')

        and: 'a Context with default logger'
        def context = Mock(Context)
        context.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }

        when: 'the api gateway adapter receives a request with an invalid form id'
        apiGatewayAdapter.handleRequest(entityRecoverRequest, context)

        then:
        thrown FormNotFoundException
    }

    void 'form config request with a valid formId, returns a valid json form config'() {
        given: 'a ApiGatewayAdapter instance'
        def apiGatewayAdapter = new FormConfigRequestApiGatewayAdapter()

        and: 'a valid stored form'
        Type type = new TypeToken<JsonFormUpdated>() {}.getType()
        JsonFormUpdated jsonFormUpdated = new Gson().fromJson(formUpdatedJson, type)
        new FormUpdatedRabbitMqAdapter().onInputEvent(jsonFormUpdated)

        and: 'a form config request input message for the already stored form'
        def entityRecoverRequest = new JsonFormConfigRequested('form-1', 'EN')

        and: 'a Context with default logger'
        def context = Mock(Context)
        context.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }

        when: 'the api gateway adapter receives a request'
        def jsonResponse = apiGatewayAdapter.handleRequest(entityRecoverRequest, context)

        then:
        jsonResponse
        noExceptionThrown()
    }

    private final static formUpdatedJson = '''{"meta":{"id":"0100badd-47ae-4f77-a0f5-10f4512ed91d","version":2,"timestamp":"1970-01-01T00:00:00","correlationId":"0","type":"FORM_UPDATED","aggregate":"Form"},"body":{"version":3,"id":"form-1","contestId":"contest-id-1","contestKey":"BBVA Open Talent 2018","formKey":"BBVA Open Talent 2018","contestName":"Open Talent 2017","legalConditionsLink":"https://www.centrodeinnovacionbbva.com/opentalent/legal","canSaveDraft":true,"languages":["EN","ES"],"defaultLanguage":"EN","showAllowShareDataCheck":false,"sendingDataMessage":[{"lang":"ES","text":"Enviando información, espere por favor..."},{"lang":"EN","text":"Sending data, wait a minute..."}],"confirmationMessage":{"header":[{"lang":"ES","text":"Hecho!"},{"lang":"EN","text":"Done!"}],"body":[{"lang":"ES","text":"Has registrado una nueva startup en Open Marketplace."},{"lang":"EN","text":"You have just registered a new Startup in the Open Marketplace."}]},"timeoutErrorMessage":{"header":[{"lang":"ES","text":"Error de conexión"},{"lang":"EN","text":"Connection error"}],"body":[{"lang":"ES","text":"Error de tiempo de conexión"},{"lang":"EN","text":"There was a connection error, please try again in a few minutes. If this error continues to occurs, please contact the platform administrators to help you resolve the problem."}]},"legalConditions":{"link":[{"lang":"ES","text":"https://openmarketplace.bbva.com/es/terms"},{"lang":"EN","text":"https://openmarketplace.bbva.com/en/terms"}],"text":[{"lang":"ES","text":"He leído y acepto"},{"lang":"EN","text":"I have read and accept the"}],"linkText":[{"lang":"ES","text":"términos legales y condiciones"},{"lang":"EN","text":"legal terms & conditions"}]},"gdpr":{"link":[{"lang":"ES","text":"https://openmarketplace.bbva.com/es/terms"},{"lang":"EN","text":"https://openmarketplace.bbva.com/en/terms"}],"text":[{"lang":"ES","text":"He leído y entiendo"},{"lang":"EN","text":"I have read and understand the"}],"linkText":[{"lang":"ES","text":"el tratamiento personal de los datos"},{"lang":"EN","text":"Personal Data Treatment"}]},"summarySentence":[{"text":[{"lang":"ES","text":"My startup "},{"lang":"EN","text":"My startup "}]},{"questionId":"omp-0-1","text":[{"lang":"ES","text":", operates in the "},{"lang":"EN","text":", operates in the "}]},{"questionId":"omp-3-1","text":[{"lang":"ES","text":" sector, in the "},{"lang":"EN","text":" sector, in the "}]},{"questionId":"omp-3-3","text":[{"lang":"ES","text":" regions, has "},{"lang":"EN","text":" regions, has "}]},{"questionId":"omp-3-12","text":[{"lang":"ES","text":" as a target audience, and offers a solution for the "},{"lang":"EN","text":" as a target audience, and offers a solution for the "}]},{"questionId":"omp-4-1","text":[{"lang":"ES","text":" area in BBVA, using a "},{"lang":"EN","text":" area in BBVA, using a "}]},{"questionId":"omp-4-3","text":[{"lang":"ES","text":" technology."},{"lang":"EN","text":" technology."}]}],"sections":[{"id":"ot18-0","text":[{"lang":"ES","text":"Registro"},{"lang":"EN","text":"Registration"}],"questions":[{"id":"ot18-0-1","type":"TEXT","required":true,"key":"APPLICATION_KEY","text":[{"lang":"ES","text":"Nombre comercial de la compañía"},{"lang":"EN","text":"Company commercial brand name"}]},{"id":"ot18-0-2","type":"EMAIL","required":true,"key":"APPLICANT_KEY","text":[{"lang":"ES","text":"Email de contacto principal"},{"lang":"EN","text":"Main contact email"}]},{"id":"ot18-summary","type":"LONGTEXT","required":false,"key":null,"text":[{"lang":"ES","text":null},{"lang":"EN","text":null}]},{"id":"ot18-shareData","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":null},{"lang":"EN","text":null}]}]},{"id":"ot18-1","text":[{"lang":"ES","text":"La Compañía"},{"lang":"EN","text":"Your Company"}],"questions":[{"id":"ot18-1-2","type":"TEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Nombre registrado"},{"lang":"EN","text":"Registered name"}]},{"id":"ot18-1-3","type":"FILE","required":true,"key":null,"text":[{"lang":"ES","text":"Logo"},{"lang":"EN","text":"Logo"}]},{"id":"ot18-1-4","type":"LINK","required":true,"key":null,"text":[{"lang":"ES","text":"Página web"},{"lang":"EN","text":"Website"}]},{"id":"ot18-1-5","type":"PHONE","required":true,"key":null,"text":[{"lang":"ES","text":"Número de teléfono"},{"lang":"EN","text":"Phone Number"}]},{"id":"ot18-1-6","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"ID de Twitter"},{"lang":"EN","text":"Twitter ID"}]},{"id":"ot18-1-7","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"LinkedIn"},{"lang":"EN","text":"LinkedIn"}]},{"id":"ot18-1-8","type":"TEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Ciudad de constitución legal"},{"lang":"EN","text":"City of legal formation"}]},{"id":"ot18-1-9","type":"COUNTRY","required":true,"key":null,"text":[{"lang":"ES","text":"País de constitución legal"},{"lang":"EN","text":"Country of legal formation"}]},{"id":"ot18-1-10","type":"DATE","required":true,"key":null,"text":[{"lang":"ES","text":"Fecha de constitución legal"},{"lang":"EN","text":"Date of legal formation"}]},{"id":"ot18-1-11","type":"COUNTRY","required":true,"key":null,"text":[{"lang":"ES","text":"Selecciona un país"},{"lang":"EN","text":"Select country"}]},{"id":"ot18-1-12","type":"SELECT","required":false,"key":null,"responses":{"type":"RANGE","options":[{"i18n":[{"lang":"EN","text":"1-5"},{"lang":"ES","text":"1-5"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki8"},{"i18n":[{"lang":"EN","text":"6-15"},{"lang":"ES","text":"6-15"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kij"},{"i18n":[{"lang":"EN","text":"16-30"},{"lang":"ES","text":"16-30"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj7"},{"i18n":[{"lang":"EN","text":"31-50"},{"lang":"ES","text":"31-50"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98jh"},{"i18n":[{"lang":"EN","text":">50"},{"lang":"ES","text":">50"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj7"}]},"text":[{"lang":"ES","text":"Número de empleados"},{"lang":"EN","text":"Number of employees"}]}]},{"id":"ot18-2","text":[{"lang":"ES","text":"Datos del Equipo"},{"lang":"EN","text":"Team member information"}],"questions":[{"id":"ot18-2-1-1","type":"TEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Nombre"},{"lang":"EN","text":"Name"}]},{"id":"ot18-2-1-2","type":"TEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Apellidos"},{"lang":"EN","text":"Surname"}]},{"id":"ot18-2-1-3","type":"DATE","required":true,"key":null,"text":[{"lang":"ES","text":"Fecha de nacimiento"},{"lang":"EN","text":"Date of birth"}]},{"id":"ot18-2-1-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"CEO"},{"lang":"ES","text":"CEO"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki9"},{"i18n":[{"lang":"EN","text":"CTO"},{"lang":"ES","text":"CTO"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kig"},{"i18n":[{"lang":"EN","text":"COO"},{"lang":"ES","text":"COO"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj8"},{"i18n":[{"lang":"EN","text":"CMO"},{"lang":"ES","text":"CMO"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98ji"},{"i18n":[{"lang":"EN","text":"CIO"},{"lang":"ES","text":"CIO"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj6"},{"i18n":[{"lang":"EN","text":"Data Scientist"},{"lang":"ES","text":"Data Scientist"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Designer"},{"lang":"ES","text":"Designer"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Advisor"},{"lang":"ES","text":"Advisor"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-akdk"},{"i18n":[{"lang":"EN","text":"Developer"},{"lang":"ES","text":"Developer"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-ldk9"},{"i18n":[{"lang":"EN","text":"Other"},{"lang":"ES","text":"Other"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-9897"}]},"text":[{"lang":"ES","text":"Puesto en la empresa"},{"lang":"EN","text":"Company role"}]},{"id":"ot18-2-1-6","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"LinkedIn"},{"lang":"EN","text":"LinkedIn"}]},{"id":"ot18-2-2-1","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Nombre"},{"lang":"EN","text":"Name"}]},{"id":"ot18-2-2-2","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Apellidos"},{"lang":"EN","text":"Surname"}]},{"id":"ot18-2-2-4","type":"EMAIL","required":false,"key":null,"text":[{"lang":"ES","text":"Email de contacto"},{"lang":"EN","text":"Contact email"}]},{"id":"ot18-2-2-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"CEO"},{"lang":"ES","text":"CEO"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki9"},{"i18n":[{"lang":"EN","text":"CTO"},{"lang":"ES","text":"CTO"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kig"},{"i18n":[{"lang":"EN","text":"COO"},{"lang":"ES","text":"COO"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj8"},{"i18n":[{"lang":"EN","text":"CMO"},{"lang":"ES","text":"CMO"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98ji"},{"i18n":[{"lang":"EN","text":"CIO"},{"lang":"ES","text":"CIO"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj6"},{"i18n":[{"lang":"EN","text":"Data Scientist"},{"lang":"ES","text":"Data Scientist"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Designer"},{"lang":"ES","text":"Designer"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Advisor"},{"lang":"ES","text":"Advisor"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-akdk"},{"i18n":[{"lang":"EN","text":"Developer"},{"lang":"ES","text":"Developer"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-ldk9"},{"i18n":[{"lang":"EN","text":"Other"},{"lang":"ES","text":"Other"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-9897"}]},"text":[{"lang":"ES","text":"Puesto en la empresa"},{"lang":"EN","text":"Company role"}]},{"id":"ot18-2-2-6","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"LinkedIn"},{"lang":"EN","text":"LinkedIn"}]},{"id":"ot18-2-3-1","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Nombre"},{"lang":"EN","text":"Name"}]},{"id":"ot18-2-3-2","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Apellidos"},{"lang":"EN","text":"Surname"}]},{"id":"ot18-2-3-4","type":"EMAIL","required":false,"key":null,"text":[{"lang":"ES","text":"Email de contacto"},{"lang":"EN","text":"Contact email"}]},{"id":"ot18-2-3-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"CEO"},{"lang":"ES","text":"CEO"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki9"},{"i18n":[{"lang":"EN","text":"CTO"},{"lang":"ES","text":"CTO"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kig"},{"i18n":[{"lang":"EN","text":"COO"},{"lang":"ES","text":"COO"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj8"},{"i18n":[{"lang":"EN","text":"CMO"},{"lang":"ES","text":"CMO"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98ji"},{"i18n":[{"lang":"EN","text":"CIO"},{"lang":"ES","text":"CIO"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj6"},{"i18n":[{"lang":"EN","text":"Data Scientist"},{"lang":"ES","text":"Data Scientist"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Designer"},{"lang":"ES","text":"Designer"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Advisor"},{"lang":"ES","text":"Advisor"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-akdk"},{"i18n":[{"lang":"EN","text":"Developer"},{"lang":"ES","text":"Developer"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-ldk9"},{"i18n":[{"lang":"EN","text":"Other"},{"lang":"ES","text":"Other"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-9897"}]},"text":[{"lang":"ES","text":"Puesto en la empresa"},{"lang":"EN","text":"Company role"}]},{"id":"ot18-2-3-6","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"LinkedIn"},{"lang":"EN","text":"LinkedIn"}]},{"id":"ot18-2-4-1","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Nombre"},{"lang":"EN","text":"Name"}]},{"id":"ot18-2-4-2","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Apellidos"},{"lang":"EN","text":"Surname"}]},{"id":"ot18-2-4-4","type":"EMAIL","required":false,"key":null,"text":[{"lang":"ES","text":"Email de contacto"},{"lang":"EN","text":"Contact email"}]},{"id":"ot18-2-4-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"CEO"},{"lang":"ES","text":"CEO"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki9"},{"i18n":[{"lang":"EN","text":"CTO"},{"lang":"ES","text":"CTO"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kig"},{"i18n":[{"lang":"EN","text":"COO"},{"lang":"ES","text":"COO"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj8"},{"i18n":[{"lang":"EN","text":"CMO"},{"lang":"ES","text":"CMO"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98ji"},{"i18n":[{"lang":"EN","text":"CIO"},{"lang":"ES","text":"CIO"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj6"},{"i18n":[{"lang":"EN","text":"Data Scientist"},{"lang":"ES","text":"Data Scientist"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Designer"},{"lang":"ES","text":"Designer"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Advisor"},{"lang":"ES","text":"Advisor"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-akdk"},{"i18n":[{"lang":"EN","text":"Developer"},{"lang":"ES","text":"Developer"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-ldk9"},{"i18n":[{"lang":"EN","text":"Other"},{"lang":"ES","text":"Other"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-9897"}]},"text":[{"lang":"ES","text":"Puesto en la empresa"},{"lang":"EN","text":"Company role"}]},{"id":"ot18-2-4-6","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"LinkedIn"},{"lang":"EN","text":"LinkedIn"}]},{"id":"ot18-2-5-1","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Nombre"},{"lang":"EN","text":"Name"}]},{"id":"ot18-2-5-2","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Apellidos"},{"lang":"EN","text":"Surname"}]},{"id":"ot18-2-5-4","type":"EMAIL","required":false,"key":null,"text":[{"lang":"ES","text":"Email de contacto"},{"lang":"EN","text":"Contact email"}]},{"id":"ot18-2-5-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"CEO"},{"lang":"ES","text":"CEO"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki9"},{"i18n":[{"lang":"EN","text":"CTO"},{"lang":"ES","text":"CTO"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kig"},{"i18n":[{"lang":"EN","text":"COO"},{"lang":"ES","text":"COO"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj8"},{"i18n":[{"lang":"EN","text":"CMO"},{"lang":"ES","text":"CMO"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98ji"},{"i18n":[{"lang":"EN","text":"CIO"},{"lang":"ES","text":"CIO"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj6"},{"i18n":[{"lang":"EN","text":"Data Scientist"},{"lang":"ES","text":"Data Scientist"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Designer"},{"lang":"ES","text":"Designer"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Advisor"},{"lang":"ES","text":"Advisor"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-akdk"},{"i18n":[{"lang":"EN","text":"Developer"},{"lang":"ES","text":"Developer"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-ldk9"},{"i18n":[{"lang":"EN","text":"Other"},{"lang":"ES","text":"Other"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-9897"}]},"text":[{"lang":"ES","text":"Puesto en la empresa"},{"lang":"EN","text":"Company role"}]},{"id":"ot18-2-5-6","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"LinkedIn"},{"lang":"EN","text":"LinkedIn"}]}]},{"id":"ot18-3","text":[{"lang":"ES","text":"Tu Negocio"},{"lang":"EN","text":"Your Business"}],"questions":[{"id":"ot18-3-1","type":"MULTISELECT","required":false,"key":null,"responses":{"type":"TAG","options":[{"i18n":[{"lang":"EN","text":"AltLending"},{"lang":"ES","text":"AltLending"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2ki2"},{"i18n":[{"lang":"EN","text":"ChatBot"},{"lang":"ES","text":"ChatBot"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-8kde"},{"i18n":[{"lang":"EN","text":"Crowdfunding"},{"lang":"ES","text":"Crowdfunding"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-kmh9"},{"i18n":[{"lang":"EN","text":"Cryptocurrencies"},{"lang":"ES","text":"Cryptocurrencies"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98gw"},{"i18n":[{"lang":"EN","text":"eCommerce"},{"lang":"ES","text":"eCommerce"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnh8"},{"i18n":[{"lang":"EN","text":"EFM (Enterprise Finance Management)"},{"lang":"ES","text":"EFM (Enterprise Finance Management)"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Identity Investment Management"},{"lang":"ES","text":"Identity Investment Management"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Lending"},{"lang":"ES","text":"Lending"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-erty"},{"i18n":[{"lang":"EN","text":"Loyalty"},{"lang":"ES","text":"Loyalty"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-aese"},{"i18n":[{"lang":"EN","text":"Microfinance"},{"lang":"ES","text":"Microfinance"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-4556"},{"i18n":[{"lang":"EN","text":"Mobile Apps"},{"lang":"ES","text":"Mobile Apps"}],"value":"daef-fgfgf34-dsdasd-gfdgdfg-e23g"},{"i18n":[{"lang":"EN","text":"mPOS"},{"lang":"ES","text":"mPOS"}],"value":"35fq-fgfgf34-dsdasd-gfdgdfg-jte3"},{"i18n":[{"lang":"EN","text":"Non Fintech"},{"lang":"ES","text":"Non Fintech"}],"value":"e343-fgfg34-dsdfasd-gfdgdfg-jyrt"},{"i18n":[{"lang":"EN","text":"Onboarding"},{"lang":"ES","text":"Onboarding"}],"value":"dewe-fgfgf34-dsdasd-gfdgdfg-iku6"},{"i18n":[{"lang":"EN","text":"Others"},{"lang":"ES","text":"Otros"}],"value":"hte3-fgfgf34-dsdasd-gfdgdfg-45yh"}]},"maxResponses":2,"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-3-2","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Otros"},{"lang":"EN","text":"Others"}]},{"id":"ot18-3-3","type":"MULTISELECT","required":false,"key":null,"responses":{"type":"TAG","options":[{"i18n":[{"lang":"EN","text":"Africa"},{"lang":"ES","text":"África"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-3424"},{"i18n":[{"lang":"EN","text":"Asia"},{"lang":"ES","text":"Asia"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-3456"},{"i18n":[{"lang":"EN","text":"Europe"},{"lang":"ES","text":"Europa"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-htft"},{"i18n":[{"lang":"EN","text":"North America"},{"lang":"ES","text":"Norteamérica"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-34tr"},{"i18n":[{"lang":"EN","text":"Oceania"},{"lang":"ES","text":"Oceanía"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-4fy8"},{"i18n":[{"lang":"EN","text":"South America"},{"lang":"ES","text":"Sudamerica"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-rtyu"},{"i18n":[{"lang":"EN","text":"Global"},{"lang":"ES","text":"Global"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-rt53"}]},"maxResponses":7,"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-3-4","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Tweet"},{"lang":"EN","text":"Tweet"}]},{"id":"ot18-3-5","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Describe tu negocio en profundidad"},{"lang":"EN","text":"Describe your business in depth"}]},{"id":"ot18-3-6","type":"LINK","required":true,"key":null,"text":[{"lang":"ES","text":"Enlace al vídeo online / Subir vídeo"},{"lang":"EN","text":"Link to online video / Upload video"}]},{"id":"ot18-3-7","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Pre-seed"},{"lang":"ES","text":"Pre-semilla"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-877j"},{"i18n":[{"lang":"EN","text":"Seed"},{"lang":"ES","text":"Semilla"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-3d4f"},{"i18n":[{"lang":"EN","text":"Fase temprana"},{"lang":"ES","text":"Early stage"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-wsq2"},{"i18n":[{"lang":"EN","text":"Growth"},{"lang":"ES","text":"Crecimiento"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-2343"},{"i18n":[{"lang":"EN","text":"Negocio maduro"},{"lang":"ES","text":"Mature business"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-cvd2"}]},"text":[{"lang":"ES","text":"Selecciona una opción"},{"lang":"EN","text":"Choose an option"}]},{"id":"ot18-3-8","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Yes"},{"lang":"ES","text":"Sí"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-877j"},{"i18n":[{"lang":"EN","text":"No"},{"lang":"ES","text":"No"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-3d4f"}]},"text":[{"lang":"ES","text":"Selecciona una opción"},{"lang":"EN","text":"Choose an option"}]},{"id":"ot18-3-9","type":"MULTISELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Proof of Concept"},{"lang":"ES","text":"Prueba de concepto"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-45f4"},{"i18n":[{"lang":"EN","text":"Pilot"},{"lang":"ES","text":"Piloto"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-mnhb"},{"i18n":[{"lang":"EN","text":"Implementation"},{"lang":"ES","text":"Implementación"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-xsaz"},{"i18n":[{"lang":"EN","text":"Servicing"},{"lang":"ES","text":"Servicios"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-rma0"}]},"maxResponses":2,"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-3-10","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Nombre de las empresas"},{"lang":"EN","text":"Company names"}]},{"id":"ot18-3-11","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Modelo de negocio"},{"lang":"EN","text":"Business model"}]},{"id":"ot18-3-12","type":"MULTISELECT","required":false,"key":null,"responses":{"type":"TAG","options":[{"i18n":[{"lang":"EN","text":"B2B"},{"lang":"ES","text":"B2B"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2ki9"},{"i18n":[{"lang":"EN","text":"Corporates"},{"lang":"ES","text":"Corporates"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-8koe"},{"i18n":[{"lang":"EN","text":"Individuals"},{"lang":"ES","text":"Individuals"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-khh9"},{"i18n":[{"lang":"EN","text":"Millennials"},{"lang":"ES","text":"Millennials"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-i8gw"},{"i18n":[{"lang":"EN","text":"P2P"},{"lang":"ES","text":"P2P"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnk8"},{"i18n":[{"lang":"EN","text":"SMEs"},{"lang":"ES","text":"SMEs"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-liq0"},{"i18n":[{"lang":"EN","text":"Wealth"},{"lang":"ES","text":"Wealth"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-pwlo"},{"i18n":[{"lang":"EN","text":"Others"},{"lang":"ES","text":"Otros"}],"value":"hte3-fgfgf34-dsdasd-gfdgdfg-ewyh"}]},"maxResponses":7,"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-3-13","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Otros"},{"lang":"EN","text":"Others"}]},{"id":"ot18-3-14","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"¿Cómo consigues llegar a nuevos clientes?"},{"lang":"EN","text":"How do you reach new clients?"}]},{"id":"ot18-3-15","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Principales competidores"},{"lang":"EN","text":"Main competitors"}]},{"id":"ot18-3-16","type":"NUMBER","required":true,"key":null,"text":[{"lang":"ES","text":"¿Cuántos usuarios activos tienes?"},{"lang":"EN","text":"How many active users do you have?"}]},{"id":"ot18-3-17","type":"SELECT","required":false,"key":null,"responses":{"type":"RANGE","options":[{"i18n":[{"lang":"EN","text":"<25,000"},{"lang":"ES","text":"<25,000"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2kt2"},{"i18n":[{"lang":"EN","text":"25,000 - 50,000"},{"lang":"ES","text":"25,000 - 50,000"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-trse"},{"i18n":[{"lang":"EN","text":"50,000 - 150,000"},{"lang":"ES","text":"50,000 - 150,000"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-weh9"},{"i18n":[{"lang":"EN","text":"150,000 - 300,000"},{"lang":"ES","text":"150,000 - 300,000"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-8765"},{"i18n":[{"lang":"EN","text":"300,000 - 500,000"},{"lang":"ES","text":"300,000 - 500,000"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-oiju"},{"i18n":[{"lang":"EN","text":"500,000 - 1M"},{"lang":"ES","text":"500,000 - 1M"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-defe"},{"i18n":[{"lang":"EN","text":"1M - 3M"},{"lang":"ES","text":"1M - 3M"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-ioue"},{"i18n":[{"lang":"EN","text":">3"},{"lang":"ES","text":">3"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-esef"}]},"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-3-18","type":"SELECT","required":false,"key":null,"responses":{"type":"RANGE","options":[{"i18n":[{"lang":"EN","text":"<25,000"},{"lang":"ES","text":"<25,000"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2kt2"},{"i18n":[{"lang":"EN","text":"25,000 - 50,000"},{"lang":"ES","text":"25,000 - 50,000"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-trse"},{"i18n":[{"lang":"EN","text":"50,000 - 150,000"},{"lang":"ES","text":"50,000 - 150,000"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-weh9"},{"i18n":[{"lang":"EN","text":"150,000 - 300,000"},{"lang":"ES","text":"150,000 - 300,000"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-8765"},{"i18n":[{"lang":"EN","text":"300,000 - 500,000"},{"lang":"ES","text":"300,000 - 500,000"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-oiju"},{"i18n":[{"lang":"EN","text":"500,000 - 1M"},{"lang":"ES","text":"500,000 - 1M"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-defe"},{"i18n":[{"lang":"EN","text":"1M - 3M"},{"lang":"ES","text":"1M - 3M"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-ioue"},{"i18n":[{"lang":"EN","text":">3"},{"lang":"ES","text":">3"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-esef"}]},"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-3-19","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Yes"},{"lang":"ES","text":"Si"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2kt2"},{"i18n":[{"lang":"EN","text":"No"},{"lang":"ES","text":"No"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-trse"}]},"text":[{"lang":"ES","text":"¿Qué tipo de inversión busca?"},{"lang":"EN","text":"What kind of investment do you seek?"}]},{"id":"ot18-3-20","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"3F (Family, Friends & Fools)"},{"lang":"ES","text":"3F (Family, Friends & Fools)"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2kt2"},{"i18n":[{"lang":"EN","text":"Business Angels"},{"lang":"ES","text":"Business Angels"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-trse"},{"i18n":[{"lang":"EN","text":"Family Office"},{"lang":"ES","text":"Family Office"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-weh9"},{"i18n":[{"lang":"EN","text":"Venture Capital"},{"lang":"ES","text":"Venture Capital"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-8765"}]},"text":[{"lang":"ES","text":"¿Qué tipo de inversión busca?"},{"lang":"EN","text":"What kind of investment do you seek?"}]}]},{"id":"ot18-4","text":[{"lang":"ES","text":"Tu Producto/Servicio"},{"lang":"EN","text":"Your Product/Service"}],"questions":[{"id":"ot18-4-1","type":"MULTISELECT","required":false,"key":null,"responses":{"type":"TAG","options":[{"i18n":[{"lang":"EN","text":"Antifraud"},{"lang":"ES","text":"Antifraud"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2ki2"},{"i18n":[{"lang":"EN","text":"Channels"},{"lang":"ES","text":"Channels"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-8kde"},{"i18n":[{"lang":"EN","text":"Compliance"},{"lang":"ES","text":"Compliance"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-kmh9"},{"i18n":[{"lang":"EN","text":"Consumer Lending"},{"lang":"ES","text":"Consumer Lending"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98gw"},{"i18n":[{"lang":"EN","text":"Corporations & Enterprises (Lending)"},{"lang":"ES","text":"Corporations & Enterprises (Lending)"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnh8"},{"i18n":[{"lang":"EN","text":"Digital Channels"},{"lang":"ES","text":"Digital Channels"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"Education/Learning"},{"lang":"ES","text":"Education/Learning"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"Financial Inclusion"},{"lang":"ES","text":"Financial Inclusion"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-erty"},{"i18n":[{"lang":"EN","text":"Foreign Exchange"},{"lang":"ES","text":"Foreign Exchange"}],"value":"jdo0-fgfgf34-dsdasd-gfdgdfg-aese"},{"i18n":[{"lang":"EN","text":"HR"},{"lang":"ES","text":"HR"}],"value":"9089-fgfgf34-dsdasd-gfdgdfg-4556"},{"i18n":[{"lang":"EN","text":"Insurance"},{"lang":"ES","text":"Insurance"}],"value":"daef-fgfgf34-dsdasd-gfdgdfg-e23g"},{"i18n":[{"lang":"EN","text":"Legal"},{"lang":"ES","text":"Legal"}],"value":"35fq-fgfgf34-dsdasd-gfdgdfg-jte3"}]},"maxResponses":2,"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-4-2","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Otros"},{"lang":"EN","text":"Others"}]},{"id":"ot18-4-3","type":"MULTISELECT","required":false,"key":null,"responses":{"type":"TAG","options":[{"i18n":[{"lang":"EN","text":"AI"},{"lang":"ES","text":"AI"}],"value":"1derg-fgfg34-dsdasd-gfddlfg-2ki2"},{"i18n":[{"lang":"EN","text":"API"},{"lang":"ES","text":"API"}],"value":"9ki7-fgfgf34-dsdasd-gfdgdfg-8kde"},{"i18n":[{"lang":"EN","text":"BigData"},{"lang":"ES","text":"BigData"}],"value":"7jug-fgfgf34-dsdasd-gfdgdfg-kmh9"},{"i18n":[{"lang":"EN","text":"Blockchain"},{"lang":"ES","text":"Blockchain"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98gw"},{"i18n":[{"lang":"EN","text":"Cloud"},{"lang":"ES","text":"Cloud"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnh8"},{"i18n":[{"lang":"EN","text":"Gamification"},{"lang":"ES","text":"Gamification"}],"value":"awd3-fgfgf34-dsdasd-gfdgdfg-lim0"},{"i18n":[{"lang":"EN","text":"IoT"},{"lang":"ES","text":"IoT"}],"value":"kjjn-fgfgf34-dsdasd-gfdgdfg-phlo"},{"i18n":[{"lang":"EN","text":"SaaS"},{"lang":"ES","text":"SaaS"}],"value":"jhgt-fgfgf34-dsdasd-gfdgdfg-erty"},{"i18n":[{"lang":"EN","text":"Others"},{"lang":"ES","text":"Otros"}],"value":"hte3-fgfgf34-dsdasd-gfdgdfg-45yh"}]},"maxResponses":2,"text":[{"lang":"ES","text":"Selecciona una o varias"},{"lang":"EN","text":"Choose one or more"}]},{"id":"ot18-4-4","type":"TEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Otros"},{"lang":"EN","text":"Others"}]},{"id":"ot18-4-5","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Tecnología en profundidad"},{"lang":"EN","text":"Technology in depth"}]},{"id":"ot18-4-12","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Describe las prácticas E2E"},{"lang":"EN","text":"E2E practices implemented"}]},{"id":"ot18-4-6","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Uso de datos"},{"lang":"EN","text":"Use of data"}]},{"id":"ot18-4-7","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Concept"},{"lang":"ES","text":"Concepto"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kij"},{"i18n":[{"lang":"EN","text":"Prototype"},{"lang":"ES","text":"Prototipo"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj7"},{"i18n":[{"lang":"EN","text":"Beta version"},{"lang":"ES","text":"Beta versión"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98jh"},{"i18n":[{"lang":"EN","text":"Ready to scale"},{"lang":"ES","text":"Listo para escalar"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj7"}]},"text":[{"lang":"ES","text":"Selecciona una opción"},{"lang":"EN","text":"Choose an option"}]},{"id":"ot18-4-8","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Describe en profundidad"},{"lang":"EN","text":"Describe in depth"}]},{"id":"ot18-4-9","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Describe en profundidad"},{"lang":"EN","text":"Describe in depth"}]},{"id":"ot18-4-10","type":"LONGTEXT","required":true,"key":null,"text":[{"lang":"ES","text":"Describe en profundidad"},{"lang":"EN","text":"Describe in depth"}]},{"id":"ot18-4-11","type":"LINK","required":true,"key":null,"text":[{"lang":"ES","text":"URL"},{"lang":"EN","text":"URL"}]}]},{"id":"ot18-5","text":[{"lang":"ES","text":"Información Adicional"},{"lang":"EN","text":"Additional Information"}],"questions":[{"id":"ot18-5-1","type":"LONGTEXT","required":false,"key":null,"text":[{"lang":"ES","text":"Logros de la compañía"},{"lang":"EN","text":"Milestones"}]},{"id":"ot18-5-2","type":"LINK","required":false,"key":null,"text":[{"lang":"ES","text":"¿Algún otro vídeo que quieras compartir?"},{"lang":"EN","text":"Any other video you would like to share?"}]},{"id":"ot18-5-3","type":"FILE","required":false,"key":null,"text":[{"lang":"ES","text":"Sube tu documento"},{"lang":"EN","text":"Upload document"}]},{"id":"ot18-5-4","type":"LONGTEXT","required":false,"key":null,"text":[{"lang":"ES","text":"¿Alguna otra información importante?"},{"lang":"EN","text":"Any other relevant information you would like to share?"}]},{"id":"ot18-5-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Alumni"},{"lang":"ES","text":"Alumni"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki8"},{"i18n":[{"lang":"EN","text":"BBVA"},{"lang":"ES","text":"BBVA"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kij"},{"i18n":[{"lang":"EN","text":"BBVA Open Talent web"},{"lang":"ES","text":"BBVA Open Talent web"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj7"},{"i18n":[{"lang":"EN","text":"BBVA Open Space web"},{"lang":"ES","text":"BBVA Open Space web"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98jh"},{"i18n":[{"lang":"EN","text":"BBVA Bancomer"},{"lang":"ES","text":"BBVA Bancomer"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj7"}]},"text":[{"lang":"ES","text":"Selecciona una opción"},{"lang":"EN","text":"Choose an option"}]}]},{"id":"omp-1","text":[{"lang":"ES","text":"Omp"},{"lang":"EN","text":"Omp"}],"helpText":[{"lang":"ES","text":"Mercado abierto"},{"lang":"EN","text":"Open MarketPlace"}],"tabs":[{"text":[{"lang":"ES","text":"Primer Tab"},{"lang":"EN","text":"First Tab"}],"helpText":[{"lang":"ES","text":"Primera"},{"lang":"EN","text":"First"}],"questions":[{"id":"omp-2-1-4","type":"TEXT","regex":"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,10}$","errorMsg":[{"text":"introduzca una dirección de email correcta","lang":"ES"},{"text":"enter a valid email address","lang":"EN"}],"required":false,"text":[{"lang":"ES","text":"Email contacto"},{"lang":"EN","text":"Contact Email"}],"hint":"example@email.com","disabled":true},{"id":"omp-2-1-5","type":"SELECT","required":false,"key":null,"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Alumni"},{"lang":"ES","text":"Alumni"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki8"},{"i18n":[{"lang":"EN","text":"BBVA"},{"lang":"ES","text":"BBVA"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8kij"},{"i18n":[{"lang":"EN","text":"BBVA Open Talent web"},{"lang":"ES","text":"BBVA Open Talent web"}],"value":"7juhg-fgfgf34-dsdasd-gfdgdfg-kmj7"},{"i18n":[{"lang":"EN","text":"BBVA Open Space web"},{"lang":"ES","text":"BBVA Open Space web"}],"value":"okj7-fgfgf34-dsdasd-gfdgdfg-98jh"},{"i18n":[{"lang":"EN","text":"BBVA Bancomer"},{"lang":"ES","text":"BBVA Bancomer"}],"value":"jhy6-fgfgf34-dsdasd-gfdgdfg-mnj7"}]},"text":[{"lang":"ES","text":"Selecciona una opción"},{"lang":"EN","text":"Choose an option"}]}]},{"text":[{"lang":"ES","text":"Segundo Tab"},{"lang":"EN","text":"Second Tab"}],"helpText":[{"lang":"ES","text":"Primera"},{"lang":"EN","text":"First"}],"questions":[{"id":"omp-2-2-4","type":"TEXT","regex":"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,10}$","errorMsg":[{"text":"introduzca una dirección de email correcta","lang":"ES"},{"text":"enter a valid email address","lang":"EN"}],"required":false,"text":[{"lang":"ES","text":"Email contacto 2"},{"lang":"EN","text":"Contact Email 2"}],"hint":"example@email.com","disabled":true},{"id":"omp-2-2-5","required":true,"type":"MULTISELECT","showExpanded":false,"label":[{"lang":"ES","text":"Define tus productos y servicios de la siguiente lista"},{"lang":"EN","text":"Define your product/service depending on the following list"}],"text":[{"lang":"ES","text":"Elige uno o más"},{"lang":"EN","text":"Choose one or more"}],"responses":{"type":"TEXT","options":[{"i18n":[{"lang":"EN","text":"Account Services & Collections"},{"lang":"ES","text":"Cuentas"}],"value":"1derg-fgfgf34-dsdasd-gfdgdfg-2ki1"},{"i18n":[{"lang":"EN","text":"Alternative Lending"},{"lang":"ES","text":"Alternativo"}],"value":"9kij7-fgfgf34-dsdasd-gfdgdfg-8ki2"}]}}]}]}]}}'''
}