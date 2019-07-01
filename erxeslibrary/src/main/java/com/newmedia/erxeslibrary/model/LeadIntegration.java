package com.newmedia.erxeslibrary.model;

import com.newmedia.erxes.basic.FormConnectMutation;
import com.newmedia.erxeslibrary.helper.Json;

import org.json.JSONObject;

public class LeadIntegration {
    private String id, name;
    private Json formData;

    public static LeadIntegration convert(FormConnectMutation.Integration integration) {
        LeadIntegration leadIntegration = new LeadIntegration();
        leadIntegration.setId(integration._id());
        leadIntegration.setName(integration.name());
        leadIntegration.setFormData(integration.formData());

        return leadIntegration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Json getFormData() {
        return formData;
    }

    public void setFormData(Json formData) {
        this.formData = formData;
    }
}
