package   com.rallydev.pusher;

import com.google.gson.Gson;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        if ( model instanceof String ) {
            return model.toString();
        }
        else {
            return gson.toJson(model);
        }
    }

}
