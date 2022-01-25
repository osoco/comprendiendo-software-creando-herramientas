package es.osoco.bbva.ats.forms.adapter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import es.osoco.bbva.ats.forms.adapter.json.JsonFormConfigRequested
import es.osoco.bbva.ats.forms.domain.exception.EntityNotFoundException
import es.osoco.bbva.ats.forms.fixtures.CreateFixtureForm
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class EntityRecoverRequestApiGatewayAdapterSpec extends Specification {

    void 'entity recover request with an invalid externalId throws an Exception'() {

        given: 'a ApiGatewayAdapter instance'
        def apiGatewayAdapter = new EntityRecoverRequestApiGatewayAdapter()
        CreateFixtureForm.createForm()

        and: 'an entity request input message'
        def jsonFormConfigRequest = new JsonFormConfigRequested("__invalid_external_id__", "0752119a-5c89-40d2-4652-ff8717f6df23")

        and: 'a Context with default logger'
        def context = Mock(Context)
        context.getLogger() >> new LambdaLogger() {
            @Override
            void log(String string) {
                System.out.print(string)
            }
        }

        when: 'the api gateway adapter receive a request'
        apiGatewayAdapter.handleRequest(jsonFormConfigRequest, context);

        then:
        thrown EntityNotFoundException
    }

}
