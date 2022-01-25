package es.osoco.bbva.ats.forms.domain.repository;

import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;

import java.util.Set;

public class FormRepository implements Repository<Form> {

    @Override
    public Form byID(String id) {
        return (Form) aggregateStore.findByKey(id);
    }

    @Override
    public void deleteKey(String key) {
        aggregateStore.deleteKey(key);
    }

    @Override
    public Set<String> findKeys(String string) {
        return null;
    }

    @Override
    public void save(Form aggregate) {
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

    public static FormRepository getInstance() {
        if (FormRepository.INSTANCE == null) {
            FormRepository.build();
        }
        return FormRepository.INSTANCE;
    }

    private static void build() {
        FormRepository.INSTANCE = new FormRepository();
    }

    private static FormRepository INSTANCE;

    private Logging logging;

    private AggregateStore aggregateStore;
}
