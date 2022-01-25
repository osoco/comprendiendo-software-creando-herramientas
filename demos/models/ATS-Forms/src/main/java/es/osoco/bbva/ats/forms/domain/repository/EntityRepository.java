package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.Entity;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class EntityRepository implements Repository<Entity>{


    @Override
    public Entity byID(String id) {
        //TODO throw new operation not available exception
        return null;
    }

    public Entity byExternalId(String id){
        return (Entity) aggregateStore.findByFieldValue("externalId", id);
    }

    @Override
    public void deleteKey(String key) {
        //TODO throw new operation not available exception
    }

    @Override
    public Set<String> findKeys(String string) {
        //TODO throw new operation not available exception
        return null;
    }

    @Override
    public void save(Entity aggregate) {
        //TODO throw new operation not available exception
    }

    @Override
    public void setAggregateStore(AggregateStore store) {
        this.aggregateStore = store;
    }

    @Override
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public static EntityRepository getInstance() {
        if (EntityRepository.INSTANCE == null) {
            EntityRepository.build();
        }
        return EntityRepository.INSTANCE;
    }

    private static void build() {
        EntityRepository.INSTANCE = new EntityRepository();
    }

    private static EntityRepository INSTANCE;

    private Logging logging;

    private AggregateStore aggregateStore;

}
