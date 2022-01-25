package es.osoco.bbva.ats.forms.domain.ports.storage;

import es.osoco.bbva.ats.forms.domain.aggregate.AggregateRoot;

import java.util.Set;

public interface AggregateStore<T extends AggregateRoot> {

    T findByKey(String key);

    Set<String> findKeys(String string);

    void save(T event);

    void deleteKey(String key);

    T findByFieldValue(String field, String value);
}
