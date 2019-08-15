package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.DialogInterface;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.LeadField;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

import java.util.ArrayList;
import java.util.List;

public class LeadAdapter extends RecyclerView.Adapter<LeadAdapter.ViewHolder> {

    private List<LeadField> leadFields;
    private SupportFragment supportFragment;

    public LeadAdapter(List<LeadField> leadFields, SupportFragment supportFragment) {
        this.leadFields = leadFields;
        this.supportFragment = supportFragment;
    }

    @Override
    public LeadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_lead_field, parent, false));
    }

    @Override
    public void onBindViewHolder(LeadAdapter.ViewHolder holder, int position) {
        holder.bind(leadFields.get(position), position);
    }

    @Override
    public int getItemCount() {
        return leadFields != null ? leadFields.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView text;
        public TextView description;
        public Spinner selectSpinner;
        public EditText input;
        public EditText textArea;
        public RecyclerView recyclerView;
        public RadioGroup radioGroup;

        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            description = itemView.findViewById(R.id.description);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            selectSpinner = itemView.findViewById(R.id.select);
            input = itemView.findViewById(R.id.input);
            textArea = itemView.findViewById(R.id.textarea);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }

        public void bind(LeadField field, int position) {
            if (!TextUtils.isEmpty(field.getText())) {
                text.setVisibility(View.VISIBLE);
                if (field.isRequired())
                    text.setText(field.getText() + "*:");
                else text.setText(field.getText() + ":");
            } else {
                text.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(field.getDescription())) {
                description.setVisibility(View.VISIBLE);
                description.setText(field.getDescription());
            } else {
                description.setVisibility(View.GONE);
            }

            if (field.getType() != null) {
                switch (field.getType()) {
                    case "select":
                        goneAll();
                        prepareSelect(field, position);
                        break;
                    case "textarea":
                        goneAll();
                        prepareTextArea(position);
                        break;
                    case "check":
                        goneAll();
                        prepareCheck(field, position);
                        break;
                    case "radio":
                        goneAll();
                        prepareRadio(field, position);
                        break;
                    default:
                        goneAll();
                        prepareInput(field, position);
                        break;
                }
            } else {
                goneAll();
                prepareInput(field, position);
            }
        }

        void goneAll() {
            selectSpinner.setVisibility(View.GONE);
            input.setVisibility(View.GONE);
            textArea.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            radioGroup.setVisibility(View.GONE);
        }

        void prepareSelect(LeadField field, final int pos) {
            selectSpinner.setVisibility(View.VISIBLE);
            final List<String> stringList = new ArrayList<>();
            stringList.add("");
            stringList.addAll(field.getOptions());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(supportFragment.getActivity(),
                    android.R.layout.simple_spinner_item, stringList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            selectSpinner.setAdapter(arrayAdapter);
            selectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        supportFragment.values[pos] = stringList.get(position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        void prepareTextArea(final int position) {
            textArea.setEnabled(true);
            textArea.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    supportFragment.values[position] = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        void prepareCheck(LeadField field, int position) {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(supportFragment.getActivity()));
            recyclerView.setHasFixedSize(true);
            CheckAdapter checkAdapter = new CheckAdapter(supportFragment, field.getOptions(), position);
            recyclerView.setAdapter(checkAdapter);
        }

        void prepareRadio(LeadField field, final int position) {
            radioGroup.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            radioGroup.setLayoutParams(layoutParams);
            RadioButton radioButton;
            for (int i = 0; i < field.getOptions().size(); i++) {
                radioButton = new RadioButton(supportFragment.getActivity());
                radioButton.setLayoutParams(layoutParams);
                radioButton.setText(field.getOptions().get(i));
                radioButton.setId(i);
                radioGroup.addView(radioButton);
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    supportFragment.setRadioValue(position,checkedId);
                }
            });
        }

        void prepareInput(final LeadField field, final int position) {
            Log.e("TAG", "prepareInput: " + position);
            input.setVisibility(View.VISIBLE);
            input.setEnabled(true);
            switch (field.getValition()) {
                case "date":
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setFocusable(false);
                    input.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            prepareInputDate(input,position);
                        }
                    });
                    break;
                case "phone":
                    input.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case "number":
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case "email":
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                default:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
            }
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    supportFragment.values[position] = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        void prepareInputDate (final EditText input, final int position) {
            FragmentManager fragmentManager = supportFragment.getActivity().getSupportFragmentManager();
            final DatePickerFragment newFragment = new DatePickerFragment();
            newFragment.show(fragmentManager, "datePicker");
            fragmentManager.executePendingTransactions();
            newFragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    input.setText(newFragment.inputText);
                    supportFragment.values[position] = newFragment.resultDate;
                }
            });
        }
    }
}
