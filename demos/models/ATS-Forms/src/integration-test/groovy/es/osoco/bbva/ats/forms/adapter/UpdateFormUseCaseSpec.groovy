package es.osoco.bbva.ats.forms.adapter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated
import spock.lang.Specification

import java.lang.reflect.Type

class UpdateFormUseCaseSpec extends Specification {


    void 'update form'() {
        given: 'a FormUpdatedRabbitMqAdapter instance'
        def rabbitMqAdapter = new FormUpdatedRabbitMqAdapter()

        and: 'form update instance'
        Gson gson = new Gson()
        Type type = new TypeToken<JsonFormUpdated>() {}.getType()
        JsonFormUpdated jsonFormUpdated = gson.fromJson(formUpdatedJson, type)

        when: 'rabbitMq adapter recieve jsonFormUpdated'
        rabbitMqAdapter.onInputEvent(jsonFormUpdated)

        then:
        noExceptionThrown()
    }

    private final static formUpdatedJson = this.getClass().getResource( "/form-1.json" ).text

}
