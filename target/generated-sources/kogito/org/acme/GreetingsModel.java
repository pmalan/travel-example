package org.acme;

import java.util.Map;
import java.util.HashMap;

@org.kie.internal.kogito.codegen.Generated(value = "kogito-codegen", reference = "greetings", name = "Greetings", hidden = false)
public class GreetingsModel implements org.kie.kogito.Model {

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap();
        return params;
    }

    @Override
    public void fromMap(Map<String, Object> params) {
        fromMap(null, params);
    }

    public void fromMap(String id, Map<String, Object> params) {
        this.id = id;
    }
}
