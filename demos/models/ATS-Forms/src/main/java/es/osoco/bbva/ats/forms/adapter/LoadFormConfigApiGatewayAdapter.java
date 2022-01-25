package es.osoco.bbva.ats.forms.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.osoco.bbva.ats.forms.adapter.infrastructure.LoggingHelper;
import es.osoco.bbva.ats.forms.adapter.json.JsonFormConfigRequested;
import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormConfig;
import es.osoco.bbva.ats.forms.application.AbstractUseCaseListener;
import es.osoco.bbva.ats.forms.application.GetFormUseCase;
import es.osoco.bbva.ats.forms.application.GetFormListener;
import es.osoco.bbva.ats.forms.application.GetFormUseCase;
import es.osoco.bbva.ats.forms.domain.exception.FormNotFoundException;
import java.util.Locale;

/**
 * API Gateway endpoint that retrieves the form matching a given id, with extra configuration.
 */
public class LoadFormConfigApiGatewayAdapter
    implements RequestHandler<JsonFormConfigRequested, String> {

    public LoadFormConfigApiGatewayAdapter() {}

    @Override
    public String handleRequest(final JsonFormConfigRequested request, final Context context) {
        String result = null;
        LoggingHelper.getInstance().initLogger(context);
        if (isWarmUpRequest(request)) {
            context.getLogger().log("Warming up");
            throw new RuntimeException("400");
        } else {
            context.getLogger().log(request.toString());
            try {
                final ResponseWrapperGetFormListener listener = new ResponseWrapperGetFormListener();
                onInputEvent(request, listener);
                result = listener.getOutput();
                if (   (result == null)
                    || ("".equals(result.trim()))) {
                    throw new FormNotFoundException(request.getFormId());
                } else if (!result.toLowerCase(Locale.getDefault()).contains(request.getFormId().toLowerCase(Locale.getDefault()))) {
                    throw new RuntimeException("Form does not match the request");
                }
            } catch (final FormNotFoundException formNotFound) {
                throw new RuntimeException("404", formNotFound);
            } catch (final Throwable unexpectedError) {
                throw new RuntimeException("500", unexpectedError);
            }
        }

        return result;
    }

    protected boolean isWarmUpRequest(final JsonFormConfigRequested request) {
        return ((request.getFormId() == null) || ("".equals(request.getFormId().trim())));
    }

    protected void onInputEvent(final JsonFormConfigRequested formRequested, final GetFormListener listener) {
        GetFormUseCase.getInstance().process(formRequested, listener);
    }

    protected static class ResponseWrapperGetFormListener
        extends AbstractUseCaseListener<JsonFormConfig>
        implements GetFormListener {

        @Override
        public void onOutputEvent(final JsonFormConfig formConfig) {
            final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
            setOutput(gson.toJson(formConfig));
        }
    }
}
