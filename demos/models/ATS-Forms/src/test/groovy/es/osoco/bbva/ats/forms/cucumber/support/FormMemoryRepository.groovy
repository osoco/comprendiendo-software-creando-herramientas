package es.osoco.bbva.ats.forms.cucumber.support

import es.osoco.bbva.ats.forms.domain.aggregate.form.Form
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore
import es.osoco.logging.Logging

class FormMemoryRepository implements AggregateStore<Form> {

    FormMemoryRepository(Logging logging) {
        this.logging = logging
    }

    Logging logging;
    List<Form> store = new ArrayList<>()

    @Override
    Form findByKey(String id) {
        store.find { it.id == id }
    }

    @Override
    Set<String> findKeys(String string) {
        return null
    }

    @Override
    void save(Form form) {
        store.add form
        form
    }

    @Override
    void deleteKey(String key) {
        store.remove(key)
    }

    @Override
    Form findByFieldValue(String field, String value) {
        return null
    }
}
