package com.newmedia.erxeslibrary.model;

import com.apollographql.apollo.api.Response;
import com.erxes.io.opens.WidgetsLeadConnectMutation;

public class FormConnect {
    private Lead lead;
    private LeadIntegration leadIntegration;

    public static FormConnect convert(Response<WidgetsLeadConnectMutation.Data> response) {
        FormConnect formConnect = new FormConnect();
        if (response.data() != null && response.data().widgetsLeadConnect() != null) {
            formConnect.setLead(Lead.convert(response.data().widgetsLeadConnect().form()));
            formConnect.setLeadIntegration(LeadIntegration.convert(response.data().widgetsLeadConnect().integration()));
        }
        return formConnect;
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead form) {
        this.lead = form;
    }

    public LeadIntegration getLeadIntegration() {
        return leadIntegration;
    }

    public void setLeadIntegration(LeadIntegration leadIntegration) {
        this.leadIntegration = leadIntegration;
    }
}
