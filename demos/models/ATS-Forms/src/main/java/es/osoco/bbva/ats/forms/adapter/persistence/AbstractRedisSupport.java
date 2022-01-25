package es.osoco.bbva.ats.forms.adapter.persistence;

import es.osoco.bbva.ats.forms.domain.aggregate.AggregateRoot;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Gives support for Redis persistence adapter.
 */
public abstract class AbstractRedisSupport<T extends AggregateRoot> implements AggregateStore<T>{

    private static final String REDIS_HOST_ENV_NAME = "REDIS_HOST";
    private static final String REDIS_PORT_ENV_NAME = "REDIS_PORT";
    private static final String REDIS_DEFAULT_HOST = "localhost";
    private static final int REDIS_DEFAULT_PORT = 6379;

    /**
     * Creates a new instance.
     */
    protected AbstractRedisSupport() {
    }


    protected static JedisPool getJedisPool() {
        if (pool == null) {
            final String redisHost = retrieveEnvVar(REDIS_HOST_ENV_NAME, REDIS_DEFAULT_HOST);
            final int redisPort = retrieveIntEnvVar(REDIS_PORT_ENV_NAME, REDIS_DEFAULT_PORT);
            pool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
        }
        return pool;
    }


    @Override
    public T findByFieldValue(String field, String value) {
        //TODO throw not valid operation exception
        return null;
    }



    private static String retrieveEnvVar(final String envVarName, final String defaultValue) {
        final String envVar = System.getenv(envVarName);
        return envVar == null ? defaultValue : envVar;
    }

    private static int retrieveIntEnvVar(final String envVarName, final int defaultValue) {
        int result = defaultValue;
        final String envVar = System.getenv(envVarName);
        if (envVar != null) {
            result = Integer.parseInt(envVar);
        }
        return result;
    }

    private static JedisPool pool;

}
