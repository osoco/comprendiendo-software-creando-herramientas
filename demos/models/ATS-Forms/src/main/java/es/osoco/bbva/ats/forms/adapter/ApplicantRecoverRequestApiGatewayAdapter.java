package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonApplicationRecoverWithRecoveryKeyRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.RecoverPartialApplicationWithRecoveryKeyUseCase;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationNotFoundException;

import java.util.Locale;

public class ApplicantRecoverRequestApiGatewayAdapter implements RequestHandler<JsonApplicationRecoverWithRecoveryKeyRequested, String> {

    public static ApplicantRecoverRequestApiGatewayAdapter getInstance() {
        return ApplicantRecoverRequestApiGatewayAdapter.ApplicantRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    protected static final class ApplicantRecoverRequestApiGatewayAdapterSingletonContainer {
        protected static ApplicantRecoverRequestApiGatewayAdapter SINGLETON = new ApplicantRecoverRequestApiGatewayAdapter();
    }

    public ApplicantRecoverRequestApiGatewayAdapter(){
        ApplicantRecoverRequestApiGatewayAdapter.ApplicantRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    private String response;

		@Override
		public String handleRequest(JsonApplicationRecoverWithRecoveryKeyRequested request, Context context) {
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

    protected boolean isWarmUpRequest(final JsonApplicationRecoverWithRecoveryKeyRequested request) {
        return (   (request.getContestId() == null)
                || ("".equals(request.getContestId().trim()))
                || (request.getApplicantKey() == null)
                || ("".equals(request.getApplicantKey().trim()))
                || (request.getApplicantRecoveryKey() == null)
                || ("".equals(request.getApplicantRecoveryKey().trim())));
    }

    public void onInputEvent(final JsonApplicationRecoverWithRecoveryKeyRequested applicationRecoverRequested) {
        RecoverPartialApplicationWithRecoveryKeyUseCase.getInstance().process(applicationRecoverRequested);
		}

		public void onOutputEvent(final JsonFormAnswered formRecovered){
		    Gson gson =new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
		    this.response = gson.toJson(formRecovered);
        }
}
