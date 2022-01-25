package es.osoco.bbva.ats.forms.application.util;

public class ConstantTimeStringEquals {

    public static boolean safeEqual(String a, String b){
        byte[] digesta = a.getBytes();
        byte[] digestb = b.getBytes();

        int result = 0;
        for (int i = 0; i < digesta.length && i < digestb.length ; i++) {
            result |= digesta[i] ^ digestb[i];
        }
        return result == 0;
    }
}
