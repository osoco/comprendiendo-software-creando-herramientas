package es.osoco.bbva.ats.forms.adapter;

public interface Adapter<T>{

    void onOutputEvent(T outputEvent);

    void onInputEvent(T outputEvent);
}
