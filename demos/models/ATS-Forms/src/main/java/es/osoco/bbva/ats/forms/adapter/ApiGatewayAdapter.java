package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;


import es.osoco.logging.LoggingFactory;
import es.osoco.logging.adapter.LoggingAdapterBuilderRegistry;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationListener;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationProducer;
import es.osoco.logging.config.LoggingConfiguration;
import es.osoco.logging.config.LoggingConfigurationRegistry;


public abstract class ApiGatewayAdapter<T> implements RequestHandler<T, Boolean>, ApplicationSubmittedPort {

		@Override
		public Boolean handleRequest(T request, Context context) {
            initLogger(context);
            Gson gson = new GsonBuilder().create();
            context.getLogger().log(gson.toJson(request));
            onInputEvent(request);
            return Boolean.TRUE;
		}


		public abstract void onInputEvent(final T externalEvent);

		private void initLogger(final Context context){
            new AwsLambdaLoggingConfigurationProducer().configureLogging(context.getLogger());
            /* Uncomment to configure logging mechanism without scanning of listeners */
            LoggingConfiguration config = LoggingConfigurationRegistry.getInstance().get("aws-lambda");
            new AwsLambdaLoggingConfigurationListener().newLoggingConfigurationAvailable(config);
		}
}
