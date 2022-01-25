package es.osoco.bbva.ats.forms.fixtures

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.osoco.bbva.ats.forms.adapter.FormUpdatedRabbitMqAdapter
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated

import java.lang.reflect.Type

class CreateFixtureForm {

    static void createForm() {
        create('form-1.json')
    }

    static void createOT18Form() {
        create('ot18-form.json')
    }

    static void createOT19Form() {
        create('ot19-form.json')
    }

    static void createOMPForm() {
        create('omp-form.json')
    }

    private static void create(String fileName) {
        def rabbitMqAdapter = new FormUpdatedRabbitMqAdapter()
        Gson gson = new Gson()
        Type type = new TypeToken<JsonFormUpdated>() {}.getType()
        JsonFormUpdated jsonFormUpdated = gson.fromJson(getFormUpdatedJson(fileName), type)
        rabbitMqAdapter.onInputEvent(jsonFormUpdated)
    }

    private static String getFormUpdatedJson(String fileName) {
        this.getClass().getResource("/$fileName").text
    }

}
