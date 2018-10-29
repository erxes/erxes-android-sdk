package com.newmedia.erxeslibrary.ui.conversations.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.FaqAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class FaqFragment extends Fragment {


    public FaqFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_faq, container, false);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new FaqAdapter(this.getContext()));
        LinearLayoutManager ln = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(ln);
        return v;
    }

}
