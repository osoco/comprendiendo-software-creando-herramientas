package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class ApplicationRepository implements Repository<Application> {

    @Override
    public Application byID(String id) {
        return aggregateStore.findByKey(id);
    }

    @Override
    public void deleteKey(String key) {
        aggregateStore.deleteKey(key);
    }

    @Override
    public Set<String> findKeys(String string) {
        return this.aggregateStore.findKeys(string);
    }

    @Override
    public void save(Application aggregate) {
        aggregateStore.save(aggregate);
    }


    @Override
    public void setAggregateStore(AggregateStore aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    @Override
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public static ApplicationRepository getInstance() {
        if (ApplicationRepository.INSTANCE == null) {
            ApplicationRepository.build();
        }
        return ApplicationRepository.INSTANCE;
    }

    private static void build() {
        ApplicationRepository.INSTANCE = new ApplicationRepository();
    }

    private static ApplicationRepository INSTANCE;

    private Logging logging;
    private AggregateStore<Application> aggregateStore;

    private ApplicationRepository(){
    }
}
