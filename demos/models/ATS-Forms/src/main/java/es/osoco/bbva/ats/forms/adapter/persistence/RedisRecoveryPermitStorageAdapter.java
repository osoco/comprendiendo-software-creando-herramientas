package es.osoco.bbva.ats.forms.adapter.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeSerializer;
import es.osoco.bbva.ats.forms.domain.aggregate.RecoveryPermit;
import redis.clients.jedis.Jedis;

import java.time.ZonedDateTime;
import java.util.Set;

public class RedisRecoveryPermitStorageAdapter extends AbstractRedisSupport<RecoveryPermit> {

    private static final String REDIS_KEY_PREFIX = "recovery-permit:";

    /**
     * Creates a new instance.
     */
    public RedisRecoveryPermitStorageAdapter() {
        super();
    }

    @Override
    public RecoveryPermit findByKey(String key) {
        String recoveryPermitJson;
        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer()).create();
        try (Jedis jedis = getJedisPool().getResource()) {
            recoveryPermitJson = jedis.get(REDIS_KEY_PREFIX + key);
        }
        return gson.fromJson(recoveryPermitJson, RecoveryPermit.class);
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
    public void save(RecoveryPermit recoveryPermit) {
        final String keyName = REDIS_KEY_PREFIX + recoveryPermit.getContestId() + ':' + recoveryPermit.getApplicantKey();
        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer()).create();
        final String recoveryPermitJson = gson.toJson(recoveryPermit);
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(keyName, recoveryPermitJson);
        }
    }

    @Override
    public void deleteKey(String key) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.del(key);
        }
    }
}
