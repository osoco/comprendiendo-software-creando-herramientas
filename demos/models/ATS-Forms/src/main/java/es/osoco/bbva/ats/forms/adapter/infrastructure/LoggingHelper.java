package es.osoco.bbva.ats.forms.adapter.infrastructure;

import com.amazonaws.services.lambda.runtime.Context;
import es.osoco.bbva.ats.forms.adapter.ExternalEvent;
import es.osoco.logging.Logging;
import es.osoco.logging.LoggingContext;
import es.osoco.logging.LoggingFactory;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationProducer;

/**
 * Simple helper to initialize AWS Lambda logging.
 */
public class LoggingHelper {

    protected LoggingHelper() {}

    public static LoggingHelper getInstance() {
        return LoggingHelperSingletonContainer.SINGLETON;
    }

    protected static final class LoggingHelperSingletonContainer {
        protected static final LoggingHelper SINGLETON = new LoggingHelper();
    }

    /**
     * Initializes the logging.
     * @param context the context.
     */
    public void initLogger(final Context context) {
        new AwsLambdaLoggingConfigurationProducer().configureLogging(context.getLogger());
    }

    /**
     * Configures the logging.
     * @param incomingEvent the event.
     */
    public void initLogger(final ExternalEvent incomingEvent) {
        final Logging logging = LoggingFactory.getInstance().createLogging();
        final LoggingContext context = logging.getLoggingContext();
        context.put("application", "ats-forms");
        context.put("useCase", this.getClass().getSimpleName());
        context.put("event", incomingEvent.getClass().getSimpleName());
    }


}
