package es.osoco.bbva.ats.forms.adapter;


import es.osoco.bbva.ats.forms.adapter.json.JsonEmailTokenRequested;
import es.osoco.bbva.ats.forms.application.EmailTokenUseCase;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

public class EmailTokenApiGatewayAdapter extends ApiGatewayAdapter<JsonEmailTokenRequested> implements ApplicationSubmittedPort {
    @Override
    public void onInputEvent(final JsonEmailTokenRequested emailTokenRequested) {
        EmailTokenUseCase.getInstance().process(emailTokenRequested);
    }
}
