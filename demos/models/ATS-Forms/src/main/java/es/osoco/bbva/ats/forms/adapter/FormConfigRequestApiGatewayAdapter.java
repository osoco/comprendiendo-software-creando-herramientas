package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.json.JsonFormConfigRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormConfig;
import es.osoco.bbva.ats.forms.application.LoadFormConfigUseCase;
import es.osoco.logging.adapter.awslambda.AwsLambdaLoggingConfigurationProducer;

/**
 * Deprecated. Do not use. Use {@link LoadFormApiGatewayAdapter} instead.
 */
@Deprecated
public class FormConfigRequestApiGatewayAdapter implements RequestHandler<JsonFormConfigRequested, String> {

    public static FormConfigRequestApiGatewayAdapter getInstance() {
        return FormConfigRequestApiGatewayAdapter.FormConfigRequestApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    protected static final class FormConfigRequestApiGatewayAdapterSingletonContainer {
        protected static FormConfigRequestApiGatewayAdapter SINGLETON = new FormConfigRequestApiGatewayAdapter();
    }

    public FormConfigRequestApiGatewayAdapter() {
        FormConfigRequestApiGatewayAdapter.FormConfigRequestApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    private String response;

    @Override
    public String handleRequest(JsonFormConfigRequested request, Context context) {
        initLogger(context);
        context.getLogger().log(request.toString());
        onInputEvent(request);
        return response;
    }

    private void initLogger(final Context context) {
        new AwsLambdaLoggingConfigurationProducer().configureLogging(context.getLogger());
    }

    private void onInputEvent(final JsonFormConfigRequested entityRecoverRequested) {
        LoadFormConfigUseCase.getInstance().process(entityRecoverRequested);
    }

    public void onOutputEvent(final JsonFormConfig formConfig) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        this.response = gson.toJson(formConfig);
    }
}
