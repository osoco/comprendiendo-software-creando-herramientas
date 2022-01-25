package es.osoco.bbva.ats.forms.adapter.persistence;

import es.osoco.bbva.ats.forms.domain.aggregate.GenericToken;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * Redis Storage Adapter implementation.
 */
public class RedisGenericTokenStorageAdapter extends AbstractRedisSupport<GenericToken> {

    private static final String REDIS_KEY_PREFIX = "generic-token:";

    private static final Integer REDIS_TTL = Integer.valueOf(System.getenv("REDIS_TOKEN_TTL"));

    /**
     * Creates a new instance.
     */
    public RedisGenericTokenStorageAdapter() {
        super();
    }

    @Override
    public GenericToken findByKey(String applicantKey) {
        GenericToken genericToken = null;

        String genericTokenKey;
        try (Jedis jedis = getJedisPool().getResource()) {
            genericTokenKey = jedis.get(REDIS_KEY_PREFIX + applicantKey);
        }
        if (genericTokenKey != null) {
            genericToken = new GenericToken(null, applicantKey, genericTokenKey);
        }
        return genericToken;
    }

    @Override
    public Set<String> findKeys(String string) {
        Set<String> tokens;

        try (Jedis jedis = getJedisPool().getResource()) {
            tokens = jedis.keys(REDIS_KEY_PREFIX + "*" + string + ":*");
        }
        return tokens;
    }

    @Override
    public void save(GenericToken genericToken) {
        //TODO cipher stored token key
        String genericTokenKey = genericToken.getToken();
        final String keyName = REDIS_KEY_PREFIX + genericToken.getApplicantKey();

        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(keyName, genericTokenKey);
            jedis.expire(keyName, REDIS_TTL);
        }
    }

    @Override
    public void deleteKey(String key) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.del(key);
        }
    }

}
