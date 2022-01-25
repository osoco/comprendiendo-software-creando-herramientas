package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.AggregateRoot;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

/**
 * @param <T> the type of the aggregate root identifier.
 */
public interface Repository<T extends AggregateRoot> {

    /**
     * Retrieves an aggregate root by its id.
     * @param id the identifier of the aggregate root to retrieve.
     * @return the aggregate root (of type AR), or {@code null} if not found.
     */
    T byID(String id);

    void deleteKey(String key);

    Set<String> findKeys(String string);

    /**
     * Persist an aggregate root
     * @param aggregate
     */
    void save(final T aggregate);


    /**
     * Specifies the {@link AggregateStore} implementation to use.
     * @param store the events store.
     */
    void setAggregateStore(AggregateStore store);

    /**
     * Specifies the logging implementation.
     * @param logging the {@link Logging} to use.
     */
    void setLogging(Logging logging);
}
