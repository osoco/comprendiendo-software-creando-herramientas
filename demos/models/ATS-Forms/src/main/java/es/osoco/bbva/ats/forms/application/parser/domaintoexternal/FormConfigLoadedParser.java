package es.osoco.bbva.ats.forms.application.parser.domaintoexternal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormConfig;
import es.osoco.bbva.ats.forms.application.parser.Parser;
import es.osoco.bbva.ats.forms.domain.events.FormConfigLoaded;
import es.osoco.logging.LoggingFactory;

public class FormConfigLoadedParser implements Parser<FormConfigLoaded> {

    public static FormConfigLoadedParser getInstance() {
        return FormConfigLoadedParser.FormConfigLoadedParserSingletonContainer.SINGLETON;
    }

    protected static final class FormConfigLoadedParserSingletonContainer {
        protected static final FormConfigLoadedParser SINGLETON = new FormConfigLoadedParser();
    }

    @Override
    public ExternalEvent toExternalEvent(FormConfigLoaded event) {
        Gson gson = new GsonBuilder().create();
        String jsonFormConfig = gson.toJson(event);
        LoggingFactory.getInstance().createLogging().info(jsonFormConfig);

        return new JsonFormConfig(event.getForm());
    }
}
