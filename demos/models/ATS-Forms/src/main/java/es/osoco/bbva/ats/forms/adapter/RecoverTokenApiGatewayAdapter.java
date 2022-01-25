package es.osoco.bbva.ats.forms.adapter;


import es.osoco.bbva.ats.forms.adapter.json.JsonTokenRecoverRequested;
import es.osoco.bbva.ats.forms.application.RecoverTokenUseCase;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

public class RecoverTokenApiGatewayAdapter extends ApiGatewayAdapter<JsonTokenRecoverRequested> implements ApplicationSubmittedPort {
    @Override
    public void onInputEvent(final JsonTokenRecoverRequested tokenRecoverRequested) {
        RecoverTokenUseCase.getInstance().process(tokenRecoverRequested);
    }
}
