package es.osoco.bbva.ats.forms.domain.service;

import es.osoco.bbva.ats.forms.domain.aggregate.Entity;
import es.osoco.bbva.ats.forms.domain.events.EntityRecoverRequested;
import es.osoco.bbva.ats.forms.domain.events.EntityRecovered;
import es.osoco.bbva.ats.forms.domain.exception.EntityNotFoundException;
import es.osoco.bbva.ats.forms.domain.repository.EntityRepository;

import java.util.function.Consumer;

public class EntityRecoverRequestedService implements Consumer<EntityRecoverRequested> {

    private static final EntityRepository entityRepository = EntityRepository.getInstance();

    @Override
    public void accept(EntityRecoverRequested entityRecoverRequested) {
        Entity entity = entityRepository.byExternalId(entityRecoverRequested.getExternalId());

        if (entity != null) {
            new EntityRecovered(entity, entityRecoverRequested.getApplicantKey()).emit();
        } else {
            throw new EntityNotFoundException();
        }
    }
}
