package com.newmedia.erxeslibrary.ui.conversations.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListAdapter;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SupportFragment} interface
 * to handle interaction events.
 * Use the {@link SupportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SupportFragment extends Fragment{
    private CircleImageView addnew_conversation;
    private RecyclerView recyclerView;
    private Config config;
    private CardView leadCardView, joinLeadCardView;
    private TextView titleLead, descriptionLead, textJoinLead;
    private ImageView imageLead;

    public SupportFragment() {
        // Required empty public constructor
    }

    public static SupportFragment newInstance() {
        SupportFragment fragment = new SupportFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Config.getInstance(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_support, container, false);
        addnew_conversation = v.findViewById(R.id.newconversation);
        addnew_conversation.setCircleBackgroundColor(config.colorCode);
        recyclerView = v.findViewById(R.id.chat_recycler_view);
        leadCardView = v.findViewById(R.id.leadCardView);
        joinLeadCardView = v.findViewById(R.id.joinLead);
        titleLead = v.findViewById(R.id.titleLead);
        descriptionLead = v.findViewById(R.id.descriptionLead);
        textJoinLead = v.findViewById(R.id.textJoinLead);
        imageLead = v.findViewById(R.id.imageLead);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        ConversationListAdapter adapter = new ConversationListAdapter(this.getContext());
        recyclerView.setAdapter(adapter);
        if( 0 == adapter.conversationList.size() && !((ConversationListActivity) getActivity()).isFirstStart){
            start_new_conversation(null);
        }

//        erxesRequest.getConversations();

        return v;
    }

    public void setLead() {
        if (isAdded() && config.formConnect != null) {
            JSONObject leadObject = config.formConnect.getLead().getCallout();
            if (leadObject != null) {
                leadCardView.setVisibility(View.VISIBLE);
                try {
                    titleLead.setText(leadObject.getString("title"));
                    descriptionLead.setText(leadObject.getString("body"));
                    textJoinLead.setText(leadObject.getString("buttonText"));
//                    joinLeadCardView.setCardBackgroundColor(Color.parseColor(config.formConnect.getLead().getThemeColor()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start_new_conversation(View v){
//        config.conversationId = null;
        ((ConversationListActivity)getActivity()).isFirstStart = true;
        Intent a = new Intent(this.getActivity(),MessageActivity.class);
        startActivity(a);
    }

}
