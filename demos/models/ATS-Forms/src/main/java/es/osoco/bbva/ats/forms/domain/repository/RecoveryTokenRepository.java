package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class RecoveryTokenRepository implements Repository<RecoveryToken> {

    @Override
    public RecoveryToken byID(String id) {
        return (RecoveryToken) aggregateStore.findByKey(id);
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
    public void save(RecoveryToken recoveryToken) {
        aggregateStore.save(recoveryToken);
    }

    @Override
    public void setAggregateStore(AggregateStore aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    @Override
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public static RecoveryTokenRepository getInstance() {
        if (RecoveryTokenRepository.INSTANCE == null) {
            RecoveryTokenRepository.build();
        }
        return RecoveryTokenRepository.INSTANCE;
    }

    private static void build() {
        RecoveryTokenRepository.INSTANCE = new RecoveryTokenRepository();
    }

    private static RecoveryTokenRepository INSTANCE;

    private Logging logging;
    private AggregateStore aggregateStore;

    private RecoveryTokenRepository(){
    }
}
