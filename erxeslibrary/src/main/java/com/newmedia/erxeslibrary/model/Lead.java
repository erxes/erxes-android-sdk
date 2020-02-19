package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.WidgetsLeadConnectMutation;
import com.newmedia.erxeslibrary.helper.Json;

import java.util.List;

public class Lead {
    private String id, title, description, buttonText, themeColor;
    private Json callout;
    private List<LeadField> fields;

    public static Lead convert(WidgetsLeadConnectMutation.Form responseForm) {
        Lead lead = new Lead();
        lead.setId(responseForm._id());
        lead.setTitle(responseForm.title());
        lead.setDescription(responseForm.description());
        lead.setButtonText(responseForm.buttonText());
//        lead.setThemeColor(responseForm.themeColor());
//        lead.setCallout(responseForm.callout());
        if (responseForm.fields() != null)
            lead.setFields(LeadField.convert(responseForm.fields()));
        return lead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public Json getCallout() {
        return callout;
    }

    public void setCallout(Json callout) {
        this.callout = callout;
    }

    public List<LeadField> getFields() {
        return fields;
    }

    public void setFields(List<LeadField> fields) {
        this.fields = fields;
    }
}
