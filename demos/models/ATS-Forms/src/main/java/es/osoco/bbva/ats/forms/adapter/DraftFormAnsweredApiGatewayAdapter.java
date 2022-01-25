package es.osoco.bbva.ats.forms.adapter;

import es.osoco.bbva.ats.forms.adapter.json.formanswered.JsonFormAnswered;
import es.osoco.bbva.ats.forms.application.SubmitApplicationDraftUseCase;
import es.osoco.bbva.ats.forms.ports.ApplicationSubmittedPort;

public class DraftFormAnsweredApiGatewayAdapter extends ApiGatewayAdapter<JsonFormAnswered> implements ApplicationSubmittedPort {
		@Override
		public void onInputEvent(final JsonFormAnswered formAnswered) {
        SubmitApplicationDraftUseCase.getInstance().process(formAnswered);
		}
}
