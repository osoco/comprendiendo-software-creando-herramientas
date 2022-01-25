package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.GenericToken;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class GenericTokenRepository implements Repository<GenericToken> {

    @Override
    public GenericToken byID(String id) {
        return (GenericToken) aggregateStore.findByKey(id);
    }

    @Override
    public void deleteKey(String key) {
        aggregateStore.deleteKey(key);
    }

    @Override
    public Set<String> findKeys(String string) {
        return aggregateStore.findKeys(string);
    }

    @Override
    public void save(GenericToken genericToken) {
        aggregateStore.save(genericToken);
    }

    @Override
    public void setAggregateStore(AggregateStore aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    @Override
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public boolean isTokenValid(String applicantKey, String recoveryKey) {
        GenericToken token = this.byID(applicantKey);
        return ((token != null) && (token.getToken().equals(recoveryKey)));
    }

    public static GenericTokenRepository getInstance() {
        if (GenericTokenRepository.INSTANCE == null) {
            GenericTokenRepository.build();
        }
        return GenericTokenRepository.INSTANCE;
    }

    private static void build() {
        GenericTokenRepository.INSTANCE = new GenericTokenRepository();
    }

    private static GenericTokenRepository INSTANCE;

    private Logging logging;
    private AggregateStore aggregateStore;

    private GenericTokenRepository() {
    }
}
