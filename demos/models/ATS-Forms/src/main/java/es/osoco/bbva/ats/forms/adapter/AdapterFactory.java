package es.osoco.bbva.ats.forms.adapter;

import es.osoco.bbva.ats.forms.adapter.json.applicationsubmitted.JsonApplicationSubmitted;
import es.osoco.bbva.ats.forms.adapter.json.emailtoken.JsonEmailTokenGenerated;
import es.osoco.bbva.ats.forms.adapter.json.newtoken.JsonNewRecoveryTokenGenerated;
import es.osoco.bbva.ats.forms.adapter.json.newtokenerror.JsonGenericTokenForNonexistentApplicantRequested;
import es.osoco.bbva.ats.forms.adapter.json.newtokenerror.JsonGenericTokenWithNoRecoverPermitRequested;
import es.osoco.logging.LoggingFactory;

import java.util.HashMap;
import java.util.Map;

public class AdapterFactory {

    private static final Map<String, Adapter> factoryAdapterByEventBuilder = new HashMap<String, Adapter>();

    static {
        try {
            String rabbitMQApplicationSubmittedEventAdapter = System.getenv("ApplicationSubmittedEventAdapter");
            String rabbitMQNewRecoveryTokenGeneratedEventAdapter = System.getenv("NewRecoveryTokenGeneratedEventAdapter");
            String rabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedEventAdapter = System.getenv("NewRecoveryTokenWithNoRecoverPermitRequestedEventAdapter");
            String rabbitMQNewRecoveryTokenForNonexistentApplicantEventAdapter = System.getenv("NewRecoveryTokenForNonexistentApplicantRequestedEventAdapter");
            if (rabbitMQApplicationSubmittedEventAdapter != null) {
                factoryAdapterByEventBuilder.put(
                    JsonApplicationSubmitted.class.getSimpleName(),
                    (Adapter) Class.forName(rabbitMQApplicationSubmittedEventAdapter).newInstance());
            }

            if (rabbitMQNewRecoveryTokenGeneratedEventAdapter != null) {
                factoryAdapterByEventBuilder.put(
                    JsonNewRecoveryTokenGenerated.class.getSimpleName(),
                    (Adapter) Class.forName(rabbitMQNewRecoveryTokenGeneratedEventAdapter).newInstance());
                factoryAdapterByEventBuilder.put(
                    JsonEmailTokenGenerated.class.getSimpleName(),
                    (Adapter) Class.forName(rabbitMQNewRecoveryTokenGeneratedEventAdapter).newInstance());
            }
            if (rabbitMQNewRecoveryTokenGeneratedEventAdapter != null) {
                factoryAdapterByEventBuilder.put(
                    JsonGenericTokenWithNoRecoverPermitRequested.class.getSimpleName(),
                    (Adapter) Class.forName(rabbitMQNewRecoveryTokenWithNoRecoverPermitRequestedEventAdapter).
                        newInstance());
            }
            if (rabbitMQNewRecoveryTokenGeneratedEventAdapter != null) {
                factoryAdapterByEventBuilder.put(
                    JsonGenericTokenForNonexistentApplicantRequested.class.getSimpleName(),
                    (Adapter) Class.forName(rabbitMQNewRecoveryTokenForNonexistentApplicantEventAdapter).
                        newInstance());
            }

        } catch (Exception ex) {
            LoggingFactory.getInstance().createLogging().error(ex.getMessage(), ex);
        }
    }

    public static Adapter getAdapter(String eventName) {
        return factoryAdapterByEventBuilder.get(eventName);
    }
}
