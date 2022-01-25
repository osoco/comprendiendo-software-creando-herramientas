package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationDataRecoverRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.ApplicationDataRecoverUseCase;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;

import java.util.Locale;

public class ApplicationDataRecoverRequestApiGatewayAdapter implements RequestHandler<JsonApplicationDataRecoverRequested, String> {

    public static ApplicationDataRecoverRequestApiGatewayAdapter getInstance() {
        return ApplicationDataRecoverRequestApiGatewayAdapter.ApplicationDataRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    protected static final class ApplicationDataRecoverRequestApiGatewayAdapterSingletonContainer {
        protected static ApplicationDataRecoverRequestApiGatewayAdapter SINGLETON = new ApplicationDataRecoverRequestApiGatewayAdapter();
    }

    public ApplicationDataRecoverRequestApiGatewayAdapter() {
        ApplicationDataRecoverRequestApiGatewayAdapter.ApplicationDataRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    private String response;

    @Override
    public String handleRequest(JsonApplicationDataRecoverRequested request, Context context) {
        String result = null;

        LoggingHelper.getInstance().initLogger(context);
        if (isWarmUpRequest(request)) {
            context.getLogger().log("Warming up");
            throw new RuntimeException("400");
        } else {
            context.getLogger().log(request.toString());
            try {
                onInputEvent(request);
                result = this.response;
                if ((result != null) && (!result.toLowerCase(Locale.getDefault()).contains(
                    request.getApplicantKey().toLowerCase(Locale.getDefault())))
                ) {
                    throw new RuntimeException("Application does not match the request");
                }
            } catch (final ApplicationNotFoundException applicationNotFoundException) {
                throw applicationNotFoundException;
            } catch (final Throwable unexpectedError) {
                throw new RuntimeException("500", unexpectedError);
            }
        }

        return result;
    }

    protected boolean isWarmUpRequest(final JsonApplicationDataRecoverRequested request) {
        return ((request.getContestId() == null)
            || ("".equals(request.getContestId().trim()))
            || (request.getApplicantKey() == null)
            || ("".equals(request.getApplicantKey().trim()))
            || (request.getApplicantRecoveryKey() == null)
            || ("".equals(request.getApplicantRecoveryKey().trim())));
    }

    public void onInputEvent(final JsonApplicationDataRecoverRequested applicationRecoverRequested) {
        ApplicationDataRecoverUseCase.getInstance().process(applicationRecoverRequested);
    }

    public void onOutputEvent(final JsonFormAnswered formRecovered) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        this.response = gson.toJson(formRecovered);
    }

}
