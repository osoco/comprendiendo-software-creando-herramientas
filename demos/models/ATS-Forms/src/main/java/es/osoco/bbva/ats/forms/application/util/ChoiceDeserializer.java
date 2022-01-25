package es.osoco.bbva.ats.forms.application.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import es.osoco.bbva.ats.forms.domain.aggregate.Choice;

import java.lang.reflect.Type;

public class ChoiceDeserializer implements JsonDeserializer<Choice> {

    @Override
    public Choice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Choice result;
        try {
            result = new Choice(null, json.getAsString());
        } catch (Throwable ex){
            JsonObject jsonObject = json.getAsJsonObject();

            if(jsonObject.get("id") == null){
                result = new Choice("","");
            } else{
                result = new Choice(
                    jsonObject.get("id").getAsString(),
                    jsonObject.get("label").getAsString());
            }
        }
        return result;
    }
}
