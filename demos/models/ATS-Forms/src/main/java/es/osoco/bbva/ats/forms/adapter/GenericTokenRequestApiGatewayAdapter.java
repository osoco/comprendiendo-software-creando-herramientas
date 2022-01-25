package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonGenericTokenGenerated;
import es.osoco.bbva.ats.forms.adapter.json.JsonGenericTokenRequested;
import es.osoco.bbva.ats.forms.application.GenerateGenericTokenUseCase;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenForNonexistentApplicantRequested;
import es.osoco.bbva.ats.forms.domain.events.GenericTokenWithNoRecoverPermitRequested;
import es.osoco.bbva.ats.forms.domain.exception.ApplicationToRecoverNotFoundException;
import es.osoco.bbva.ats.forms.domain.exception.DataRecoveryNotAllowedException;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

public class GenericTokenRequestApiGatewayAdapter implements ApplicationSubmittedPort, RequestHandler<JsonGenericTokenRequested, String> {

    public static GenericTokenRequestApiGatewayAdapter getInstance() {
        return GenericTokenRequestApiGatewayAdapter.GenericTokenRequestApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    @Override
    public String handleRequest(JsonGenericTokenRequested request, Context context) {
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
            } catch (final ApplicationToRecoverNotFoundException applicationToRecoverNotFoundException) {
                context.getLogger().log("Application to recover not found");
                new GenericTokenForNonexistentApplicantRequested(
                    applicationToRecoverNotFoundException.getApplicantKey(),
                    applicationToRecoverNotFoundException.getContestId(),
                    applicationToRecoverNotFoundException.getLanguage()
                ).emit();
                return this.response;
            } catch (final DataRecoveryNotAllowedException dataRecoveryNotAllowedException) {
                context.getLogger().log("Data Recovery Not Allowed");
                new GenericTokenWithNoRecoverPermitRequested(
                    dataRecoveryNotAllowedException.getApplicantKey(),
                    dataRecoveryNotAllowedException.getContestId(),
                    dataRecoveryNotAllowedException.getLanguage()
                ).emit();
                return this.response;
            } catch (final Throwable unexpectedError) {
                throw new RuntimeException("500", unexpectedError);
            }
        }

        return result;
    }

    protected static final class GenericTokenRequestApiGatewayAdapterSingletonContainer {
        protected static GenericTokenRequestApiGatewayAdapter SINGLETON = new GenericTokenRequestApiGatewayAdapter();
    }

    public GenericTokenRequestApiGatewayAdapter() {
        GenericTokenRequestApiGatewayAdapter.GenericTokenRequestApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    protected boolean isWarmUpRequest(final JsonGenericTokenRequested request) {
        return ((request.getContestId() == null)
            || ("".equals(request.getContestId().trim()))
            || (request.getLanguage() == null)
            || ("".equals(request.getLanguage().trim()))
            || (request.getApplicantKey() == null)
            || ("".equals(request.getApplicantKey().trim())));
    }

    private String response;

    public void onInputEvent(final JsonGenericTokenRequested jsonGenericTokenRequested) {
        GenerateGenericTokenUseCase.getInstance().process(jsonGenericTokenRequested);
    }

    public void onOutputEvent(final JsonGenericTokenGenerated jsonGenericTokenGenerated) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        this.response = gson.toJson(jsonGenericTokenGenerated);
    }
}











