package es.osoco.bbva.ats.forms.domain.config;

import es.osoco.bbva.ats.forms.domain.aggregate.AggregateRoot;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.bbva.ats.forms.domain.repository.*;
import es.osoco.logging.Logging;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


public class DomainConfig {

    @SuppressWarnings("unchecked")
    public <AR extends AggregateRoot> void configure(Repository<AR> repository) {
        repository.setAggregateStore(this.aggregateStoreAdapters.get(repository.getClass()));
        repository.setLogging(this.logging);
    }

    public static DomainConfig getInstance() {
        if (DomainConfig.INSTANCE == null) {
            throw new MissingDomainConfigException();
        }
        return DomainConfig.INSTANCE;
    }

    public static void setInstance(DomainConfig config) {
				DomainConfig.INSTANCE = config;
		}

    public static class Builder {

        private Map<Class<?>, AggregateStore> aggregateStoreAdapters;

        private Logging loggingAdapter;

        public Builder() {
            aggregateStoreAdapters = new HashMap<>();
        }

        public Builder applicationAggregateStoreAdapter(AggregateStore store) {
            this.aggregateStoreAdapters.put(ApplicationRepository.class, store);
            return this;
        }

        public Builder formAggregateStoreAdapter(AggregateStore store) {
            this.aggregateStoreAdapters.put(FormRepository.class, store);
            return this;
        }

        public Builder tokenAggregateStoreAdapter(AggregateStore store) {
            this.aggregateStoreAdapters.put(RecoveryTokenRepository.class, store);
            return this;
        }

        public Builder emailTokenAggregateStoreAdapter(AggregateStore store) {
            this.aggregateStoreAdapters.put(EmailTokenRepository.class, store);
            return this;
        }

        public Builder genericTokenAggregateStoreAdapter(AggregateStore store){
            this.aggregateStoreAdapters.put(GenericTokenRepository.class, store);
            return this;
        }

        public Builder entityAggregateStroreAdapter(AggregateStore store){
            this.aggregateStoreAdapters.put(EntityRepository.class, store);
            return this;
        }

        public Builder recoveryPermissionStoreAdapter(AggregateStore store){
            this.aggregateStoreAdapters.put(RecoveryPermitRepository.class, store);
            return this;
        }

        public Builder logging(final Logging logging) {
            this.loggingAdapter = logging;
            return this;
        }
        public Map<Class<?>, AggregateStore> getAggregateStoreAdapters() {
            return this.aggregateStoreAdapters;
        }

        public DomainConfig build() {
            DomainConfig config = new DomainConfig(this);
            DomainConfig.setInstance(config);
            return config;
        }
    }

    @SuppressWarnings("unchecked")
    private DomainConfig(Builder builder) {
        // TODO: Throws IllegalStateException if the DomainConfig invariants cannot be satisfied
        this.aggregateStoreAdapters = builder.getAggregateStoreAdapters();
        configureRepositories();
    }

    private void configureRepositories() {
        this.aggregateStoreAdapters.keySet().forEach(key -> {
            try {
                configure(getRepositoryInstance(key));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }

    private Repository getRepositoryInstance(Class<?> key) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (Repository) key.getDeclaredMethod("getInstance").invoke(null);
    }

    private Map<Class<?>, AggregateStore> aggregateStoreAdapters;
    private Logging logging;

    private static DomainConfig INSTANCE;
}
