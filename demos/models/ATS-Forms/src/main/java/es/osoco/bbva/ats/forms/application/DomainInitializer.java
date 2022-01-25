package es.osoco.bbva.ats.forms.application;

import es.osoco.bbva.ats.forms.adapter.persistence.RedisApplicationStorageAdapter;
import es.osoco.bbva.ats.forms.adapter.persistence.RedisFormStorageAdapter;
import es.osoco.bbva.ats.forms.adapter.persistence.RedisGenericTokenStorageAdapter;
import es.osoco.bbva.ats.forms.adapter.persistence.RedisRecoveryPermitStorageAdapter;
import es.osoco.bbva.ats.forms.adapter.persistence.RedisTokenStorageAdapter;
import es.osoco.bbva.ats.forms.adapter.persistence.RedisEmailTokenStorageAdapter;
import es.osoco.bbva.ats.forms.adapter.persistence.RoundFilterApiEntityAdapter;
import es.osoco.bbva.ats.forms.domain.config.DomainConfig;

public class DomainInitializer {
    protected DomainInitializer() {}

    public static DomainInitializer getInstance() {
        return DomainInitializerSingletonContainer.SINGLETON;
    }

    protected static final class DomainInitializerSingletonContainer {
        protected static final DomainInitializer SINGLETON = new DomainInitializer();
    }

    /**
     * Initializes the domain adapters.
     * @param incomingEvent the event.
     * @return the {@link DomainConfig} instance.
     */
    public DomainConfig domainInit() {

        return
            new DomainConfig.Builder()
                .applicationAggregateStoreAdapter(new RedisApplicationStorageAdapter())
                .formAggregateStoreAdapter(new RedisFormStorageAdapter())
                .tokenAggregateStoreAdapter(new RedisTokenStorageAdapter())
                .entityAggregateStroreAdapter(new RoundFilterApiEntityAdapter())
                .genericTokenAggregateStoreAdapter(new RedisGenericTokenStorageAdapter())
                .recoveryPermissionStoreAdapter(new RedisRecoveryPermitStorageAdapter())
                .emailTokenAggregateStoreAdapter(new RedisEmailTokenStorageAdapter())
                .build();
    }
}
