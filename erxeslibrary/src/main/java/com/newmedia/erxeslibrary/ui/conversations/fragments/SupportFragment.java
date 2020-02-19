package com.newmedia.erxeslibrary.ui.conversations.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.erxes.io.opens.type.FieldValueInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.LeadAdapter;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;

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
    private Config config;
    private CardView leadCardView, getLeadCardView, joinLeadCardView, getJoinLeadCardView;
    private TextView titleLead, getTitleLead, descriptionLead, getDescriptionLead, textJoinLead, getTextJoinLead;
    private ImageView imageLead;
    private RecyclerView getRecyclerView;
    public Boolean[] booleans;
    public String[] resultChecks;

    public String[] values;

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
        getRecyclerView = v.findViewById(R.id.getRecyclerView);
        leadCardView = v.findViewById(R.id.leadCardView);
        getLeadCardView = v.findViewById(R.id.getLeadCardView);
        joinLeadCardView = v.findViewById(R.id.joinLead);
        getJoinLeadCardView = v.findViewById(R.id.getJoinLead);
        titleLead = v.findViewById(R.id.titleLead);
        getTitleLead = v.findViewById(R.id.getTitleLead);
        descriptionLead = v.findViewById(R.id.descriptionLead);
        getDescriptionLead = v.findViewById(R.id.getDescriptionLead);
        textJoinLead = v.findViewById(R.id.textJoinLead);
        getTextJoinLead = v.findViewById(R.id.getTextJoinLead);
        imageLead = v.findViewById(R.id.imageLead);

//        setLead();

        if (config.messengerdata.isShowChat()) {
            chatContainer.setVisibility(View.VISIBLE);
            addnewConversation = v.findViewById(R.id.newconversation);
            LinearLayout newConversationClick = v.findViewById(R.id.newConversationCLick);
            recyclerView = v.findViewById(R.id.chat_recycler_view);
            initIcon();
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

            ConversationListAdapter adapter = new ConversationListAdapter(this.getActivity(), config.conversations);
            recyclerView.setAdapter(adapter);
            if (0 == adapter.conversationList.size() && !config.isFirstStart) {
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
        Glide.with(this).load(config.getPlusIcon(getActivity(),0))
                .optionalCircleCrop()
                .into(addnewConversation);
    }

    public void setLead() {
        if (isAdded() && config.formConnect != null) {
//            Json leadObject = config.formConnect.getLead().getCallout();
//            if (leadObject != null) {
//                leadCardView.setVisibility(View.VISIBLE);
//                getLeadCardView.setVisibility(View.GONE);
//                if (!TextUtils.isEmpty(leadObject.getString("featuredImage")) &&
//                        leadObject.getString("featuredImage").contains("http")) {
//                    Glide.with(this)
//                            .load(leadObject.getString("featuredImage"))
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(imageLead);
//                } else {
//                    imageLead.setVisibility(View.GONE);
//                }
//                titleLead.setText(leadObject.getString("title"));
//                descriptionLead.setText(leadObject.getString("body"));
//                textJoinLead.setText(leadObject.getString("buttonText"));
////                    joinLeadCardView.setCardBackgroundColor(Color.parseColor(config.formConnect.getLead().getThemeColor()));
//                initLead();
//            }
        }
    }

    public void setLeadAgain() {
        getLeadCardView.setVisibility(View.GONE);
        leadCardView.setVisibility(View.VISIBLE);
        titleLead.setText(config.formConnect.getLead().getTitle());
        descriptionLead.setText(config.formConnect.getLeadIntegration().getLeadData().getString("thankContent"));
        textJoinLead.setText(R.string.Create_new);
        imageLead.setVisibility(View.GONE);
//                    joinLeadCardView.setCardBackgroundColor(Color.parseColor(config.formConnect.getLead().getThemeColor()));
    }

    private void initLead() {
        joinLeadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leadCardView.setVisibility(View.GONE);
                getLeadCardView.setVisibility(View.VISIBLE);
                initGetLead();
            }
        });
    }

    private void initGetLead() {
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

        values = new String[config.formConnect.getLead().getFields().size()];

        getRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        getRecyclerView.setHasFixedSize(true);
        LeadAdapter leadAdapter = new LeadAdapter(config.formConnect.getLead().getFields(), this);
        getRecyclerView.setAdapter(leadAdapter);

        getTextJoinLead.setText(config.formConnect.getLead().getButtonText());
        getJoinLeadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRequired();
            }
        });
    }

    public void setCheckValue(int parentPosition) {
        if (booleans != null && booleans.length > 0) {
            int size = 0;
            for (Boolean aBoolean : booleans) {
                if (aBoolean)
                    size++;
            }
            resultChecks = new String[size];
            int pointer = 0;
            for (int i = 0; i < booleans.length; i++) {
                if (booleans[i]) {
                    resultChecks[pointer++] = config.formConnect.getLead().getFields().get(parentPosition).getOptions().get(i);
                }
            }
        }
    }

    public void setRadioValue(int parentPosition, int checkedPosition) {
        values[parentPosition] = config.formConnect.getLead().getFields().get(parentPosition).getOptions().get(checkedPosition);
    }

    private void checkRequired() {
        boolean isDone = true;
        int position = 0;
        for (int i = 0; i < config.formConnect.getLead().getFields().size(); i++) {
            if (config.formConnect.getLead().getFields().get(i).isRequired() &&
                    TextUtils.isEmpty(values[i])) {
                position = i;
                isDone = false;
                break;
            }
        }
        if (isDone) {
            sendLead();
        } else {
            if (config.formConnect.getLead().getFields().get(position).getType() != null) {
                if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("select")) {
                    getRecyclerView.findViewHolderForAdapterPosition(position)
                            .itemView
                            .findViewById(R.id.select)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("textarea")) {
                    getRecyclerView.findViewHolderForAdapterPosition(position)
                            .itemView
                            .findViewById(R.id.textarea)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                } else if (config.formConnect.getLead().getFields().get(position).getType().equalsIgnoreCase("input")) {
                    getRecyclerView.findViewHolderForAdapterPosition(position)
                            .itemView
                            .findViewById(R.id.textarea)
                            .setBackgroundResource(R.drawable.rounded_bg_error);
                }
            } else {
                getRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView
                        .findViewById(R.id.input)
                        .setBackgroundResource(R.drawable.rounded_bg_error);
            }
        }
    }

    private void sendLead() {
        if (config.fieldValueInputs.size() > 0) {
            config.fieldValueInputs.clear();
        }
        for (int i = 0; i < config.formConnect.getLead().getFields().size(); i++) {
//            if (!TextUtils.isEmpty(values[i])) {
                FieldValueInput fieldValueInput = FieldValueInput.builder()
                        ._id(config.formConnect.getLead().getFields().get(i).getId())
                        .type(config.formConnect.getLead().getFields().get(i).getType())
                        .validation(config.formConnect.getLead().getFields().get(i).getValition())
                        .text(config.formConnect.getLead().getFields().get(i).getText())
                        .value(values[i])
                        .build();
                config.fieldValueInputs.add(fieldValueInput);
//            }
        }

        ((ConversationListActivity) getActivity()).sendLead();
    }

    public void start_new_conversation() {
        config.isFirstStart = true;
        Intent a = new Intent(this.getActivity(), MessageActivity.class);
        startActivity(a);
    }

}
