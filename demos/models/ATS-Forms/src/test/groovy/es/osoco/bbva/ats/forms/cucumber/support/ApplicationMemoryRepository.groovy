package es.osoco.bbva.ats.forms.cucumber.support

import es.osoco.bbva.ats.forms.domain.aggregate.Application
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore
import es.osoco.logging.Logging

class ApplicationMemoryRepository implements AggregateStore<Application> {

    ApplicationMemoryRepository(Logging logging) {
        this.logging = logging
    }

    Logging logging;
    List<Application> store = new ArrayList<>()

    @Override
    Application findByKey(String id) {
        store.find { it.id == id }
    }

    @Override
    Set<String> findKeys(String string) {
        return null
    }

    @Override
    void save(Application application) {
        store.add application
        application
    }

    @Override
    void deleteKey(String key) {
        store.remove(key)
    }

    @Override
    Application findByFieldValue(String field, String value) {
        return null
    }
}
