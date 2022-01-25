package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryPermit;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class RecoveryPermitRepository implements Repository<RecoveryPermit> {

    @Override
    public RecoveryPermit byID(String id) {
        return (RecoveryPermit) aggregateStore.findByKey(id);
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
    public void save(RecoveryPermit recoveryPermit) {
        aggregateStore.save(recoveryPermit);
    }

    @Override
    public void setAggregateStore(AggregateStore aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    @Override
    public void setLogging(Logging logging) {
        this.logging = logging;
    }


    public static RecoveryPermitRepository getInstance() {
        if (RecoveryPermitRepository.INSTANCE == null) {
            RecoveryPermitRepository.build();
        }
        return RecoveryPermitRepository.INSTANCE;
    }

    private static void build() {
        RecoveryPermitRepository.INSTANCE = new RecoveryPermitRepository();
    }

    private static RecoveryPermitRepository INSTANCE;
    private Logging logging;
    private AggregateStore aggregateStore;

    private RecoveryPermitRepository() {

    }

}
