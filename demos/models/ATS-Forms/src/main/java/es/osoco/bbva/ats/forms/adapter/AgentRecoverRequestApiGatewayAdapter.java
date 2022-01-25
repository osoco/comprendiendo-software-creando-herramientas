package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationRecoverRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.RecoverPartialApplicationUseCase;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;

import java.util.Locale;

public class AgentRecoverRequestApiGatewayAdapter implements RequestHandler<JsonApplicationRecoverRequested, String> {

    public static AgentRecoverRequestApiGatewayAdapter getInstance() {
        return AgentRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    protected static final class AgentRecoverRequestApiGatewayAdapterSingletonContainer {
        protected static AgentRecoverRequestApiGatewayAdapter SINGLETON = new AgentRecoverRequestApiGatewayAdapter();
    }

    public AgentRecoverRequestApiGatewayAdapter() {
        AgentRecoverRequestApiGatewayAdapter.AgentRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    private String response;

    @Override
    public String handleRequest(JsonApplicationRecoverRequested request, Context context) {
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
                if (   (result != null)
                    && (!result.toLowerCase(Locale.getDefault()).contains(request.getApplicantKey().toLowerCase(Locale.getDefault())))) {
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

    protected boolean isWarmUpRequest(final JsonApplicationRecoverRequested request) {
        return (   (request.getContestId() == null)
                || ("".equals(request.getContestId().trim()))
                || (request.getApplicantKey() == null)
                || ("".equals(request.getApplicantKey().trim())));
    }

    private void onInputEvent(final JsonApplicationRecoverRequested applicationRecoverRequested) {
        RecoverPartialApplicationUseCase.getInstance().process(applicationRecoverRequested);
    }

    public void onOutputEvent(final JsonFormAnswered formRecovered){
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        this.response = gson.toJson(formRecovered);
    }
}
