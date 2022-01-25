package es.osoco.bbva.ats.forms.adapter.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.application.util.ChoiceDeserializer;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeDeserializer;
import es.osoco.bbva.ats.forms.application.util.ZonedDateTimeSerializer;
import es.osoco.bbva.ats.forms.domain.aggregate.Application;
import es.osoco.bbva.ats.forms.domain.aggregate.Choice;
import redis.clients.jedis.Jedis;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Redis Storage Adapter implementation.
 */
public class RedisApplicationStorageAdapter extends AbstractRedisSupport<Application> {

    private static final String REDIS_KEY_PREFIX = "application:";
    /**
     * Creates a new instance.
     */
    public RedisApplicationStorageAdapter() {
        super();
    }

    @Override
    public Application findByKey(String key) {
        Application application;
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class,new ZonedDateTimeSerializer())
                .registerTypeAdapter(ZonedDateTime.class,new ZonedDateTimeDeserializer())
                .registerTypeAdapter(Choice.class, new ChoiceDeserializer())
                .create();

        try (Jedis jedis = getJedisPool().getResource()) {
            String json = jedis.get(REDIS_KEY_PREFIX + key);
            if (json == null){
                json = jedis.get(key);
            }
            application = gson.fromJson(json, Application.class);
        }

        return application;
    }

    @Override
    public Set<String> findKeys(String string) {
        Set<String> keys;

        try (Jedis jedis = getJedisPool().getResource()) {
            keys = jedis.keys( REDIS_KEY_PREFIX + "*" + string + "*");
        };
        return keys;
    }

    @Override
    public void save(Application application) {
        //TODO cipher stored application data
        final String keyName = REDIS_KEY_PREFIX + application.getId();
        final Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(ZonedDateTime.class,new ZonedDateTimeSerializer())
                .registerTypeAdapter(ZonedDateTime.class,new ZonedDateTimeDeserializer())
                .create();

        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.set(keyName, gson.toJson(application));
        }
    }

    @Override
    public void deleteKey(String key) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.del(key);
        }
    }
}
