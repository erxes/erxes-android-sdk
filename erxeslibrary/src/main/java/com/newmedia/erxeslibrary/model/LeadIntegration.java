package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.WidgetsLeadConnectMutation;
import com.newmedia.erxeslibrary.helper.Json;

public class LeadIntegration {
    private String id, name;
    private Json leadData;

    public static LeadIntegration convert(WidgetsLeadConnectMutation.Integration integration) {
        LeadIntegration leadIntegration = new LeadIntegration();
        leadIntegration.setId(integration._id());
        leadIntegration.setName(integration.name());
        leadIntegration.setLeadData(integration.leadData());

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

    public Json getLeadData() {
        return leadData;
    }

    public void setLeadData(Json formData) {
        this.leadData = formData;
    }
}
