package es.osoco.bbva.ats.forms.adapter.persistence;

import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryToken;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * Redis Storage Adapter implementation.
 */
public class RedisTokenStorageAdapter extends AbstractRedisSupport<RecoveryToken> {

    private static final String REDIS_KEY_PREFIX = "token:";

    private static final Integer REDIS_TTL = Integer.valueOf(System.getenv("REDIS_TOKEN_TTL"));

    /**
     * Creates a new instance.
     */
    public RedisTokenStorageAdapter() {
    }

    @Override
    public RecoveryToken findByKey(String key) {
        RecoveryToken recoveryToken = null;
        String contestId = key.split(":")[0];
        String applicantKey = key.split(":")[1];

        String recoveryKey;
        try (Jedis jedis = getJedisPool().getResource()) {
            recoveryKey = jedis.get(REDIS_KEY_PREFIX +contestId + ":" + applicantKey);
        }
        if (recoveryKey != null){
            recoveryToken = new RecoveryToken(contestId, null, applicantKey, recoveryKey);
        }
        return recoveryToken;
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
    public void save(RecoveryToken recoveryToken) {
        //TODO cipher stored token key
        String recoveryKey = recoveryToken.getRecoveryKey();
        final String keyName = REDIS_KEY_PREFIX + recoveryToken.getId();

        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(keyName, recoveryKey);
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
