package es.osoco.bbva.ats.forms.adapter;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.SubmitEntityUseCase;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

public class EntitySubmitApiGatewayAdapter extends ApiGatewayAdapter<JsonFormAnswered> implements ApplicationSubmittedPort {
		@Override
		public void onInputEvent(final JsonFormAnswered formAnwered) {
        SubmitEntityUseCase.getInstance().process(formAnwered);
		}
}
