package com.newmedia.erxeslibrary.ui.conversations.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListAdapter;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SupportFragment} interface
 * to handle interaction events.
 * Use the {@link SupportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SupportFragment extends Fragment {
    private CircleImageView addnew_conversation;
    private RecyclerView recyclerView;
    private Config config;

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        ConversationListAdapter adapter = new ConversationListAdapter(this.getContext());
        recyclerView.setAdapter(adapter);
        if( 0 == adapter.conversationList.size() ){
            start_new_conversation(null);
        }
//        erxesRequest.getConversations();
        return v;
    }



    public void start_new_conversation(View v){
//        config.conversationId = null;
        Intent a = new Intent(this.getActivity(),MessageActivity.class);
        startActivity(a);
    }

}
