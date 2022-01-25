package es.osoco.bbva.ats.forms.domain.aggregate.form

import es.osoco.bbva.ats.forms.application.parser.externaltodomain.FormParser
import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated

import com.google.gson.Gson

import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings('JavaIoPackageAccess')
class FormSpec
    extends Specification {

    @Unroll
    def "The id of the form is formId:LANGUAGE"() {
        when:
        final FormParser formParser = FormParser.getInstance()
        final JsonFormUpdated event =
            new Gson().fromJson(
                new File(file).text, JsonFormUpdated.class)
        List<Form> forms = formParser.createForms(event);

        then:
            forms.each { form -> assert form.getId() == form.getFormId() + ":" + form.getLanguage().toUpperCase() }

        where:
        file << [
            'src/test/resources/form.updated.1.json',
            'src/test/resources/form.updated.2.json',
            'src/test/resources/form.updated.3.json'
        ]
    }
}
