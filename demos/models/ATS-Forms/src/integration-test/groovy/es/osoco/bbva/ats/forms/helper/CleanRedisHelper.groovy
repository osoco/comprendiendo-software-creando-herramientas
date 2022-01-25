package es.osoco.bbva.ats.forms.helper

import es.osoco.bbva.ats.forms.adapter.persistence.AbstractRedisSupport
import redis.clients.jedis.Jedis

class CleanRedisHelper {

    static cleanRedis(){
        Jedis jedis = AbstractRedisSupport.getJedisPool().getResource()
        jedis.flushAll();
    }
}
