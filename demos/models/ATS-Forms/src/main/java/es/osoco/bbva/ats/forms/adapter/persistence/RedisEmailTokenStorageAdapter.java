package es.osoco.bbva.ats.forms.adapter.persistence;

import es.osoco.bbva.ats.forms.domain.aggregate.EmailToken;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * Redis Storage Adapter implementation.
 */
public class RedisEmailTokenStorageAdapter extends AbstractRedisSupport<EmailToken> {

    private static final String REDIS_KEY_PREFIX = "emailtoken:";

    private static final Integer REDIS_TTL = Integer.valueOf(System.getenv("REDIS_TOKEN_TTL"));

    /**
     * Creates a new instance.
     */
    public RedisEmailTokenStorageAdapter() {
    }

    @Override
    public EmailToken findByKey(String key) {
        EmailToken emailToken = null;
        String applicantKey = key.split(":")[0];
        String recoveryKey = key.split(":")[1];

        try (Jedis jedis = getJedisPool().getResource()) {
            recoveryKey = jedis.get(REDIS_KEY_PREFIX + applicantKey + ":" + recoveryKey);
        }
        if (recoveryKey != null){
            emailToken = new EmailToken(null, null, applicantKey, recoveryKey);
        }
        return emailToken;
    }

    @Override
    public Set<String> findKeys(String applicantKeyString) {
        Set<String> tokens;

        try (Jedis jedis = getJedisPool().getResource()) {
            tokens = jedis.keys(REDIS_KEY_PREFIX + "*" + applicantKeyString + "*");
        }
        return tokens;
    }

    @Override
    public void save(EmailToken emailToken) {
        //TODO cipher stored token key
        String emailTokenKey = emailToken.getEmailToken();
        final String keyName = REDIS_KEY_PREFIX + emailToken.getId();

        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(keyName, emailTokenKey);
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
