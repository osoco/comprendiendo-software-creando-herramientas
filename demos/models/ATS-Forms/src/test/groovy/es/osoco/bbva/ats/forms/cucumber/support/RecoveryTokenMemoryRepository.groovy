package es.osoco.bbva.ats.forms.cucumber.support

import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore
import es.osoco.logging.Logging

import java.util.stream.Collectors

class RecoveryTokenMemoryRepository implements AggregateStore<RecoveryToken> {

    RecoveryTokenMemoryRepository(Logging logging) {
        this.logging = logging
    }

    Logging logging;
    List<RecoveryToken> store = new ArrayList<>()

    @Override
    RecoveryToken findByKey(String id) {
        store.find { it.id == id }
    }

    @Override
    Set<String> findKeys(String string) {
        store.stream().filter{token -> token.applicantKey == string}.map{token -> token.getRecoveryKey()}.collect(Collectors.toSet())
    }

    @Override
    void save(RecoveryToken recoveryToken) {
        store.add recoveryToken
        recoveryToken
    }

    @Override
    void deleteKey(String key) {
        store.remove(key)
    }

    @Override
    RecoveryToken findByFieldValue(String field, String value) {
        return null
    }
}
