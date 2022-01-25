package es.osoco.bbva.ats.forms.adapter;

import es.osoco.bbva.ats.forms.adapter.json.formupdated.JsonFormUpdated;
import es.osoco.bbva.ats.forms.application.UpdateFormUseCase;

public class FormUpdatedRabbitMqAdapter extends ApiGatewayAdapter<JsonFormUpdated>{

    @Override
    public void onInputEvent(final JsonFormUpdated jsonFormUpdated) {
        UpdateFormUseCase.getInstance().process(jsonFormUpdated);
    }
}
