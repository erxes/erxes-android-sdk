package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.WidgetsLeadConnectMutation;

import java.util.ArrayList;
import java.util.List;

public class LeadField {

    private String id, formId, type, check, text, description, name, valition;
    private List<String> options = new ArrayList<>();
    private boolean isRequired = false;
    private int order = 0;

    public static List<LeadField> convert(List<WidgetsLeadConnectMutation.Field> fields) {
        List<LeadField> leadFieldList = new ArrayList<>();
        for (WidgetsLeadConnectMutation.Field field : fields) {
            LeadField leadField = new LeadField();
            leadField.setId(field._id());
            leadField.setType(field.type());
            leadField.setText(field.text());
            leadField.setDescription(field.description());
            leadField.setName(field.name());
            leadField.setValition(field.validation());
            if (field.order() != null)
                leadField.setOrder(field.order());
            if (field.isRequired() != null)
                leadField.setRequired(field.isRequired());
            leadField.setOptions(field.options());
            leadFieldList.add(leadField);
        }
        return leadFieldList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValition() {
        return valition;
    }

    public void setValition(String valition) {
        this.valition = valition;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
