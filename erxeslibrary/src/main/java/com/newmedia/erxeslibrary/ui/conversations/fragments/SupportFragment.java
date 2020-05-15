package com.newmedia.erxeslibrary.ui.conversations.fragments;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.erxes.io.opens.type.FieldValueInput;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.SoftKeyboard;
import com.newmedia.erxeslibrary.model.LeadField;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.CheckAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.DatePickerFragment;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SupportFragment} interface
 * to handle interaction events.
 * Use the {@link SupportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SupportFragment extends Fragment {
    private ImageView addnewConversation;
    public RecyclerView recyclerView;
    private ViewGroup formFieldsLayout, parentLayout;
    private Config config;
    private CardView getLeadCardView, getJoinLeadCardView;
    private TextView getTitleLead, getDescriptionLead, getTextJoinLead;
    public String[] values;
    private SoftKeyboard softKeyboard;
    private boolean isInitedForm = false;

    public SupportFragment() {
    }

    public static SupportFragment newInstance() {
        return new SupportFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Config.getInstance(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_support, container, false);
        CardView chatContainer = v.findViewById(R.id.chatContainer);
        getLeadCardView = v.findViewById(R.id.getLeadCardView);
        getJoinLeadCardView = v.findViewById(R.id.getJoinLead);
        getTitleLead = v.findViewById(R.id.getTitleLead);
        getDescriptionLead = v.findViewById(R.id.getDescriptionLead);
        getTextJoinLead = v.findViewById(R.id.getTextJoinLead);
        parentLayout = v.findViewById(R.id.parentLayout);
        formFieldsLayout = v.findViewById(R.id.formFieldsLayout);

        if (config.messengerdata.isShowChat()) {
            chatContainer.setVisibility(View.VISIBLE);
            addnewConversation = v.findViewById(R.id.newconversation);
            LinearLayout newConversationClick = v.findViewById(R.id.newConversationCLick);
            recyclerView = v.findViewById(R.id.chat_recycler_view);
            initIcon();
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

            ConversationListAdapter adapter = new ConversationListAdapter(this.getActivity(), config.conversations);
            recyclerView.setAdapter(adapter);
            setLead();

            if (0 == adapter.conversationList.size()) {
                start_new_conversation();
            }

            newConversationClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start_new_conversation();
                }
            });

        }

        return v;
    }

    private void initIcon() {
        Glide.with(this).load(config.getPlusIcon(getActivity(), 0))
                .optionalCircleCrop()
                .into(addnewConversation);
    }

    protected void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"
            };
            int requestCode = 200;

            requestPermissions(permissions, requestCode);
        }
    }

    public void setLead() {
        if (!isHidden() && config != null && config.formConnect != null && !isInitedForm) {
            isInitedForm = true;
            askPermissions();
            getLeadCardView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(config.formConnect.getLead().getTitle())) {
                getTitleLead.setText(config.formConnect.getLead().getTitle());
            } else {
                getTitleLead.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(config.formConnect.getLead().getDescription())) {
                getDescriptionLead.setText(config.formConnect.getLead().getDescription());
            } else {
                getDescriptionLead.setVisibility(View.GONE);
            }
            getTextJoinLead.setText(config.formConnect.getLead().getButtonText());
            getJoinLeadCardView.setCardBackgroundColor(config.colorCode);

            values = new String[config.formConnect.getLead().getFields().size()];

            for (int i = 0; i < config.formConnect.getLead().getFields().size(); i++)
                addField(config.formConnect.getLead().getFields().get(i), i);

            softKeyboard = new SoftKeyboard(parentLayout, (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE));
            softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
                @Override
                public void onSoftKeyboardHide() {
                    ((ConversationListActivity) getActivity()).onKeyboardHide();
                }

                @Override
                public void onSoftKeyboardShow() {
                    ((ConversationListActivity) getActivity()).onKeyboardShow();
                }
            });

            getJoinLeadCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkRequired();
                }
            });
        }
    }

    private void addField(LeadField field, int position) {
        View itemView = getLayoutInflater().inflate(R.layout.item_lead_field, null);
        TextView text = itemView.findViewById(R.id.text);
        TextView description = itemView.findViewById(R.id.description);
        TextView file = itemView.findViewById(R.id.file);
        RadioGroup radioGroup = itemView.findViewById(R.id.radioGroup);
        Spinner selectSpinner = itemView.findViewById(R.id.select);
        EditText input = itemView.findViewById(R.id.input);
        EditText textArea = itemView.findViewById(R.id.textarea);
        RecyclerView checkList = itemView.findViewById(R.id.checkList);

        if (!TextUtils.isEmpty(field.getText())) {
            text.setVisibility(View.VISIBLE);
            if (field.isRequired())
                text.setText(field.getText() + " *:");
            else text.setText(field.getText() + " :");
        } else {
            text.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(field.getDescription())) {
            description.setVisibility(View.VISIBLE);
            description.setText(field.getDescription());
        } else {
            description.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(field.getType())) {
            switch (field.getType()) {
                case "select":
                    prepareSelect(field, position, selectSpinner);
                    break;
                case "textarea":
                    prepareTextArea(position, textArea);
                    break;
                case "check":
                    prepareCheck(field, position, checkList);
                    break;
                case "radio":
                    prepareRadio(field, position, radioGroup);
                    break;
                case "file":
                    prepareFile(field, position, file);
                    break;
                default:
                    prepareInput(field, position, input);
                    break;
            }
        } else {
            prepareInput(field, position, input);
        }

        formFieldsLayout.addView(itemView);
    }

    void prepareFile(LeadField field, final int pos, TextView fileText) {
        fileText.setVisibility(View.VISIBLE);
        fileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(pos);
                ((ConversationListActivity) getActivity()).onBrowseLead(fileText);
            }
        });
    }

    void prepareSelect(LeadField field, final int pos, Spinner selectSpinner) {
        selectSpinner.setVisibility(View.VISIBLE);
        final List<String> stringList = new ArrayList<>();
        stringList.add("");
        stringList.addAll(field.getOptions());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.select_dropdown_view, stringList) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextSize(14);
                if (values[pos] != null && values[pos].equalsIgnoreCase(tv.getText().toString())) {
                    tv.setBackgroundColor(config.colorCode);
                    tv.setTextColor(config.getInColor(config.colorCode));
                } else {
                    tv.setBackgroundColor(Color.parseColor("#ffffff"));
                    tv.setTextColor(Color.parseColor("#000000"));
                }
                return tv;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextSize(14);
                return tv;
            }
        };
        selectSpinner.setAdapter(arrayAdapter);
        selectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    values[pos] = stringList.get(position);
                    selectSpinner.setBackgroundResource(R.drawable.rounded_input);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    void prepareTextArea(final int position, EditText textArea) {
        textArea.setVisibility(View.VISIBLE);
        textArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                values[position] = s.toString();
                textArea.setBackgroundResource(R.drawable.rounded_input);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void prepareCheck(LeadField field, int position, RecyclerView checkList) {
        checkList.setVisibility(View.VISIBLE);
        checkList.setLayoutManager(new LinearLayoutManager(getActivity()));
        checkList.setHasFixedSize(true);
        CheckAdapter checkAdapter = new CheckAdapter(this, field.getOptions(), position, checkList);
        checkList.setAdapter(checkAdapter);
    }

    void prepareRadio(LeadField field, final int position, RadioGroup radioGroup) {
        radioGroup.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        radioGroup.setLayoutParams(layoutParams);
        RadioButton radioButton;
        for (int i = 0; i < field.getOptions().size(); i++) {
            radioButton = new RadioButton(getActivity());
            radioButton.setLayoutParams(layoutParams);
            radioButton.setText(field.getOptions().get(i));
            radioButton.setId(i);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, // unchecked
                            new int[]{android.R.attr.state_checked}, // checked
                    },
                    new int[]{
                            Color.parseColor("#000000"),
                            ((ConversationListActivity) getActivity()).config.colorCode,
                    }
            );
            CompoundButtonCompat.setButtonTintList(radioButton, colorStateList);
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setRadioValue(position, checkedId);
                radioGroup.setBackgroundResource(R.drawable.rounded_input);
            }
        });
    }

    void prepareInput(final LeadField field, final int position, EditText input) {
        config.setCursorColor(input,config.colorCode);
        input.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(field.getValition()))
            switch (field.getValition()) {
                case "date":
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setFocusable(false);
                    input.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            prepareInputDate(input, position);
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
        else {
            switch (field.getType()) {
                case "phone":
                    input.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case "email":
                    input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                default:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
            }
        }
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                values[position] = s.toString();
                input.setBackgroundResource(R.drawable.rounded_input);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void prepareInputDate(EditText input, final int position) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        final DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(fragmentManager, "datePicker");
        fragmentManager.executePendingTransactions();
        newFragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                input.setText(newFragment.inputText);
                values[position] = newFragment.resultDate;
                input.setBackgroundResource(R.drawable.rounded_input);
            }
        });
    }

    public void setCheckValue(int parentPosition) {
        StringBuilder checkResult = new StringBuilder();
        boolean isAppendedFirst = false;
        RecyclerView checkList = formFieldsLayout.getChildAt(parentPosition).findViewById(R.id.checkList);
        if (checkList.getAdapter() != null) {
            for (int i = 0; i < checkList.getAdapter().getItemCount(); i++) {
                CheckBox checkBox = (CheckBox) checkList.getChildAt(i);
                if (checkBox.isChecked()) {
                    if (!isAppendedFirst) {
                        checkResult.append(config.formConnect.getLead().getFields().get(parentPosition).getOptions().get(i));
                        isAppendedFirst = true;
                    } else {
                        checkResult.append(",");
                        checkResult.append(config.formConnect.getLead().getFields().get(parentPosition).getOptions().get(i));
                    }
                }
            }
            values[parentPosition] = checkResult.toString();
        }
    }

    public void setRadioValue(int parentPosition, int checkedPosition) {
        values[parentPosition] = config.formConnect.getLead().getFields().get(parentPosition).getOptions().get(checkedPosition);
    }

    private void checkRequired() {
        boolean isDone = true;
        int position = 0;
        for (int i = 0; i < config.formConnect.getLead().getFields().size(); i++) {
            if (config.formConnect.getLead().getFields().get(i).isRequired()) {
                if (TextUtils.isEmpty(values[i])) {
                    Toast.makeText(getActivity(), "Required", Toast.LENGTH_SHORT).show();
                    position = i;
                    isDone = false;
                    break;
                } else {
                    if (config.formConnect.getLead().getFields().get(i).getType() != null) {
                        if (config.formConnect.getLead().getFields().get(i).getType().equalsIgnoreCase("textarea") ||
                                config.formConnect.getLead().getFields().get(i).getType().equalsIgnoreCase("input")) {
                            if (config.formConnect.getLead().getFields().get(i).getValition() != null) {
                                if (config.formConnect.getLead().getFields().get(i).getValition().equalsIgnoreCase("email")) {
                                    if (!config.isValidEmail(values[i])) {
                                        Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_SHORT).show();
                                        position = i;
                                        isDone = false;
                                        break;
                                    }
                                }
                                if (config.formConnect.getLead().getFields().get(i).getValition().equalsIgnoreCase("phone")) {
                                    if (values[i].length() < 8) {
                                        Toast.makeText(getActivity(), "Invalid phone", Toast.LENGTH_SHORT).show();
                                        position = i;
                                        isDone = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (config.formConnect.getLead().getFields().get(i).getType().equalsIgnoreCase("email")) {
                            if (!config.isValidEmail(values[i])) {
                                Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_SHORT).show();
                                position = i;
                                isDone = false;
                                break;
                            }
                        }
                        if (config.formConnect.getLead().getFields().get(i).getType().equalsIgnoreCase("phone")) {
                            if (values[i].length() < 8) {
                                Toast.makeText(getActivity(), "Invalid phone", Toast.LENGTH_SHORT).show();
                                position = i;
                                isDone = false;
                                break;
                            }
                        }
                    } else {
                        if (config.formConnect.getLead().getFields().get(i).getValition().equalsIgnoreCase("email")) {
                            if (!config.isValidEmail(values[i])) {
                                Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_SHORT).show();
                                position = i;
                                isDone = false;
                                break;
                            }
                        }
                        if (config.formConnect.getLead().getFields().get(i).getValition().equalsIgnoreCase("phone")) {
                            if (values[i].length() < 8) {
                                Toast.makeText(getActivity(), "Invalid phone", Toast.LENGTH_SHORT).show();
                                position = i;
                                isDone = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (isDone) {
            sendLead();
        } else {
            if (config.formConnect.getLead().getFields().get(position).getType() != null) {
                if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("select")) {
                    formFieldsLayout.getChildAt(position).findViewById(R.id.select)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("textarea")) {
                    formFieldsLayout.getChildAt(position).findViewById(R.id.textarea)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("radio")) {
                    formFieldsLayout.getChildAt(position).findViewById(R.id.radioGroup)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("check")) {
                    formFieldsLayout.getChildAt(position).findViewById(R.id.checkList)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("file")) {
                    formFieldsLayout.getChildAt(position).findViewById(R.id.file)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else {
                    formFieldsLayout.getChildAt(position).findViewById(R.id.input)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                }
            } else {
                formFieldsLayout.getChildAt(position).findViewById(R.id.input)
                        .setBackgroundResource(R.drawable.rounded_bg_error);
            }
            parentLayout.scrollTo(0, (int) formFieldsLayout.getChildAt(position).getY());
        }
    }

    private void sendLead() {
        if (config.fieldValueInputs.size() > 0) {
            config.fieldValueInputs.clear();
        }
        for (int i = 0; i < config.formConnect.getLead().getFields().size(); i++) {
            FieldValueInput fieldValueInput = FieldValueInput.builder()
                    ._id(config.formConnect.getLead().getFields().get(i).getId())
                    .type(config.formConnect.getLead().getFields().get(i).getType())
                    .validation(config.formConnect.getLead().getFields().get(i).getValition())
                    .text(config.formConnect.getLead().getFields().get(i).getText())
                    .value(values[i])
                    .build();
            config.fieldValueInputs.add(fieldValueInput);
        }

        ((ConversationListActivity) getActivity()).sendLead();
    }

    public void setLeadThank() {
        getJoinLeadCardView.setVisibility(View.GONE);
        formFieldsLayout.removeAllViews();
        TextView textView = (TextView) getLayoutInflater().inflate(R.layout.autolink_textview,null);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        if (config.formConnect.getLeadIntegration().getLeadData().getString("thankContent") != null)
            textView.setText(config.formConnect.getLeadIntegration().getLeadData().getString("thankContent"));
        formFieldsLayout.addView(textView);
    }

    public void start_new_conversation() {
        Intent a = new Intent(this.getActivity(), MessageActivity.class);
        startActivity(a);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        softKeyboard.closeSoftKeyboard();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }
}
