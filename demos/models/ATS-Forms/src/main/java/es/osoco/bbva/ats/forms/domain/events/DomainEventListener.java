package es.osoco.bbva.ats.forms.domain.events;


public interface DomainEventListener {

		void onEvent(DomainEvent event);

}
