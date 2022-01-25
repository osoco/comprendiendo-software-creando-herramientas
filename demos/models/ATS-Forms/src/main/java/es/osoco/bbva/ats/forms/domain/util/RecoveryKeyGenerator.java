package es.osoco.bbva.ats.forms.domain.util;

import java.util.UUID;

public class RecoveryKeyGenerator {

    public static String generateKey(){
        return String.valueOf(UUID.randomUUID());
    }
}
