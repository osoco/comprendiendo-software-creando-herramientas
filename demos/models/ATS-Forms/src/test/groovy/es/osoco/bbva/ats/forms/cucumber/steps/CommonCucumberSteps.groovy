package es.osoco.bbva.ats.forms.cucumber.steps

import cucumber.api.DataTable
import cucumber.api.java.en.And
import es.osoco.bbva.ats.forms.cucumber.support.AutomationApi
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form
import es.osoco.bbva.ats.forms.domain.events.FormUpdated
import org.hamcrest.MatcherAssert

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue

class CommonCucumberSteps {

    AutomationApi helper = AutomationApi.instance

    FormUpdated formUpdated

    @And('^a Form with the following properties:$')
    void and_a_form_with_the_following_properties(DataTable dataTable) throws Throwable {

        Map<String, String> formMap = dataTable.asMap(String.class, String.class)
        formUpdated = helper.createForm(
                formMap.get('formId'),
                formMap.get('language'),
                formMap.get('sections')
        )

        assertThat(formUpdated.getForm(), is(notNullValue(Form.class)))
        helper.formStore.save(formUpdated.getForm())
        assert helper.formStore.findByKey(formUpdated.getForm().getId()) != null

        MatcherAssert.assertThat(formUpdated.getForm().getSections(), is(notNullValue()))
        MatcherAssert.assertThat(formUpdated.getForm().getFormId(), is(notNullValue()))
        helper.setFixture("form", formMap)
    }
}
