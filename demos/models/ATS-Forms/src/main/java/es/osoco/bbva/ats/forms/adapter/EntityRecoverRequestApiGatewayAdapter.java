package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonEntityRecoverRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonEntityFormAnswered;
import es.osoco.bbva.ats.forms.application.RecoverEntityUseCase;
import es.osoco.bbva.ats.forms.domain.exception.EntityNotFoundException;
import java.util.Locale;

public class EntityRecoverRequestApiGatewayAdapter implements RequestHandler<JsonEntityRecoverRequested, String> {

    public static EntityRecoverRequestApiGatewayAdapter getInstance() {
        return EntityRecoverRequestApiGatewayAdapter.EntityRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON;
    }

    protected static final class EntityRecoverRequestApiGatewayAdapterSingletonContainer {
        protected  static EntityRecoverRequestApiGatewayAdapter SINGLETON = new EntityRecoverRequestApiGatewayAdapter();
    }

    public EntityRecoverRequestApiGatewayAdapter() {
        EntityRecoverRequestApiGatewayAdapter.EntityRecoverRequestApiGatewayAdapterSingletonContainer.SINGLETON = this;
    }

    private String response;

    @Override
    public String handleRequest(JsonEntityRecoverRequested request, Context context) {
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
                    throw new RuntimeException("Entity does not match the request");
                }
            } catch (final EntityNotFoundException entityNotFound) {
                throw new RuntimeException("404", entityNotFound);
            } catch (final Throwable unexpectedError) {
                throw new RuntimeException("500", unexpectedError);
            }
        }

        return result;
    }

    protected boolean isWarmUpRequest(final JsonEntityRecoverRequested request) {
        return (   (request.getExternalId() == null)
                || ("".equals(request.getExternalId().trim()))
                || (request.getApplicantKey() == null)
                || ("".equals(request.getApplicantKey().trim())));
    }

    private void onInputEvent(final JsonEntityRecoverRequested entityRecoverRequested) {
        RecoverEntityUseCase.getInstance().process(entityRecoverRequested);
    }

    public void onOutputEvent(final JsonEntityFormAnswered formAnswered){
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        this.response = gson.toJson(formAnswered);
    }

}
