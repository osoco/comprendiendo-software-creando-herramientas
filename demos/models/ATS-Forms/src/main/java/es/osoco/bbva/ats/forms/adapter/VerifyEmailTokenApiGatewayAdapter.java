package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonEmailTokenVerified;
import es.osoco.bbva.ats.forms.adapter.json.JsonVerifyEmailTokenRequested;
import es.osoco.bbva.ats.forms.application.VerifyEmailTokenUseCase;
import es.osoco.bbva.ats.forms.domain.events.NonExistingEmailTokenRequested;
import es.osoco.bbva.ats.forms.domain.exception.EmailTokenNotFoundException;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

import es.osoco.logging.Logging;
import es.osoco.logging.LoggingFactory;
import es.osoco.logging.adapter.LoggingAdapterBuilderRegistry;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationListener;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationProducer;
import es.osoco.logging.config.LoggingConfiguration;
import es.osoco.logging.config.LoggingConfigurationRegistry;

public class VerifyEmailTokenApiGatewayAdapter implements ApplicationSubmittedPort, RequestHandler<JsonVerifyEmailTokenRequested, String> {

    public VerifyEmailTokenApiGatewayAdapter() {
        VerifyEmailTokenApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    public static VerifyEmailTokenApiGatewayAdapter getInstance() {
        return VerifyEmailTokenApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    protected static final class VerifyEmailTokenApiGatewayAdapterSingletonContainer {
        protected static VerifyEmailTokenApiGatewayAdapter SINGLETON = new VerifyEmailTokenApiGatewayAdapter();
    }

    @Override
    public String handleRequest(JsonVerifyEmailTokenRequested request, Context context) {
        String result = null;
        initLogger(context);
        LoggingHelper.getInstance().initLogger(context);
        if (isWarmUpRequest(request)) {
            context.getLogger().log("Warming up with request: " + request);
            throw new RuntimeException("400");
        } else {
            context.getLogger().log(request.toString());
            try {
                onInputEvent(request);
            } catch (final EmailTokenNotFoundException emailTokenNotFoundException) {
                context.getLogger().log("Email token " + emailTokenNotFoundException.getEmailToken() + " not found");
                throw emailTokenNotFoundException;
            } catch (final Throwable unexpectedError) {
                throw new RuntimeException("500", unexpectedError);
            }
        }
        return this.response;
    }

		private void initLogger(final Context context){
        new AwsLambdaLoggingConfigurationProducer().configureLogging(context.getLogger());
        /* Uncomment to configure logging mechanism without scanning of listeners */
        LoggingConfiguration config = LoggingConfigurationRegistry.getInstance().get("aws-lambda");
        new AwsLambdaLoggingConfigurationListener().newLoggingConfigurationAvailable(config);
		}

    protected boolean isWarmUpRequest(final JsonVerifyEmailTokenRequested request) {
        return ((request.getEmailToken() == null)
                || ("".equals(request.getEmailToken().trim())));
    }

    public void onInputEvent(final JsonVerifyEmailTokenRequested jsonVerifyEmailTokenRequested) {
        VerifyEmailTokenUseCase.getInstance().process(jsonVerifyEmailTokenRequested);
    }

    public void onOutputEvent(final JsonEmailTokenVerified jsonEmailTokenVerified) {
        Logging logging = LoggingFactory.getInstance().createLogging();
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        this.response = gson.toJson(jsonEmailTokenVerified);
    }

    private String response;
}











