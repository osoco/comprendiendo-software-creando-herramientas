package es.osoco.bbva.ats.forms.adapter;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.UpdateApplicationUseCase;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

public class UpdateApplicationApiGatewayAdapter extends ApiGatewayAdapter<JsonFormAnswered> implements ApplicationSubmittedPort {
		@Override
		public void onInputEvent(final JsonFormAnswered formAnswered) {
        UpdateApplicationUseCase.getInstance().process(formAnswered);
		}
}
