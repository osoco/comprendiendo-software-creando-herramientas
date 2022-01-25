package es.osoco.bbva.ats.forms.adapter.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeSerializer;
import es.osoco.bbva.ats.forms.domain.aggregate.form.Form;
import redis.clients.jedis.Jedis;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Redis Storage Adapter implementation.
 */
public class RedisFormStorageAdapter extends AbstractRedisSupport<Form>{

    private static final String REDIS_KEY_PREFIX = "form:";

    /**
     * Creates a new instance.
     */
    public RedisFormStorageAdapter() {
    }

    @Override
    public Form findByKey(String key) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,new ZonedDateTimeSerializer()).create();
        String formJson;
        try (Jedis jedis = getJedisPool().getResource()) {
            formJson = jedis.get(REDIS_KEY_PREFIX + key);
        }
        return gson.fromJson(formJson, Form.class);
    }

    @Override
    public Set<String> findKeys(String string) {
        Set<String> keys;

        try (Jedis jedis = getJedisPool().getResource()) {
            keys = jedis.keys(REDIS_KEY_PREFIX + string + ":*");
        }
        return keys;
    }


    @Override
    public void save(Form form) {
        final String keyName = REDIS_KEY_PREFIX + form.getId();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,new ZonedDateTimeSerializer()).create();
        final String formJson = gson.toJson(form);

        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(keyName, formJson);
        }
    }

    @Override
    public void deleteKey(String key) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.del(key);
        }
    }
}
