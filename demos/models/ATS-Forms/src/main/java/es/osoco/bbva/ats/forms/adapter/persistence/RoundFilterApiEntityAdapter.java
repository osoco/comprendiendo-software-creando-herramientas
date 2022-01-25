package es.osoco.bbva.ats.forms.adapter.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.osoco.bbva.ats.forms.domain.aggregate.Entity;
import es.osoco.bbva.ats.forms.domain.ports.storage.AggregateStore;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis Storage Adapter implementation.
 */
public class RoundFilterApiEntityAdapter implements AggregateStore<Entity> {

    final private Logging logging = LoggingFactory.getInstance().createLogging();

    /**
     * Creates a new instance.
     */
    public RoundFilterApiEntityAdapter() {
    }


    private static CloseableHttpClient HTTPCLIENT = HttpClients.createDefault();

    private static final String ENTITY_ENDPOINT = "entities";

    private static final String ROUNDFILTER_HOST_ENV_NAME = "ROUNDFILTER_HOST";
    private static final String ROUNDFILTER_PORT_ENV_NAME = "ROUNDFILTER_PORT";
    private static final String ROUNDFILTER_DEFAULT_HOST = "http://localhost";
    private static final String ROUNDFILTER_DEFAULT_PORT = "8888";

    private static final String ROUNDFILTER_API_URL =
            retrieveEnvVar(ROUNDFILTER_HOST_ENV_NAME,ROUNDFILTER_DEFAULT_HOST)
            +":"
            +retrieveEnvVar(ROUNDFILTER_PORT_ENV_NAME,ROUNDFILTER_DEFAULT_PORT);

    private static String retrieveEnvVar(final String envVarName, final String defaultValue) {
        final String envVar = System.getenv(envVarName);
        return envVar == null ? defaultValue : envVar;
    }


    @Override
    public Entity findByKey(String key) {
        //TODO throw new not valid operation Exception
        return null;
    }

    @Override
    public Set<String> findKeys(String string) {
        //TODO throw new not valid operation Exception
        return null;
    }

    @Override
    public void save(Entity event) {
        //TODO throw new not valid operation Exception
    }

    @Override
    public void deleteKey(String key) {
        //TODO throw new not valid operation Exception
    }

    @Override
    public Entity findByFieldValue(String field, String value) {
        final String url = ROUNDFILTER_API_URL+"/"+ENTITY_ENDPOINT+"?"+field+"="+value;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("account", "BBVA_OPEN_TALENT");

        try {
            CloseableHttpResponse response = HTTPCLIENT.execute(httpGet);
            HttpEntity entity = response.getEntity();

            String content = EntityUtils.toString(entity);
            logging.info("Roundfilter response code: " + response.getStatusLine().getStatusCode());
            logging.info("Roundfilter response body: " + content);
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Entity>>(){}.getType();
            List<Entity> entities = gson.fromJson(content, listType);
            return entities.get(0);
        } catch (IOException e) {
            logging.error("Error accessing " + url + ":" + e.getMessage(), e);
            e.printStackTrace();
            // TODO create custom expcetion
            //            throw new RuntimeException(e.getMessage());
        }
        return null;
    }
}
