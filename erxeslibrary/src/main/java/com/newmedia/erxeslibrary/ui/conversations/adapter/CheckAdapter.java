package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

import java.util.Arrays;
import java.util.List;

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.ViewHolder> {

    private List<String> options;
    private SupportFragment supportFragment;
    private Boolean[] booleans;
    private int parentPosition;

    CheckAdapter(SupportFragment supportFragment, List<String> options, int parentPosition) {
        this.supportFragment = supportFragment;
        this.options = options;
        this.parentPosition = parentPosition;

        int size = 0;
        if (options != null && options.size() > 0) {
            size = options.size();
        }
        booleans = new Boolean[size];
        Arrays.fill(booleans, Boolean.FALSE);
    }

    @Override
    public CheckAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_check_lead, parent, false));
    }

    @Override
    public void onBindViewHolder(CheckAdapter.ViewHolder holder, int position) {
        holder.bind(options.get(position), position);
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.item_checkbox);
        }

        public void bind(String option, final int position) {
            checkBox.setText(option);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    booleans[position] = !booleans[position];
                    supportFragment.booleans = booleans;
                    supportFragment.setCheckValue(parentPosition);
                    supportFragment.values[parentPosition] = Arrays.toString(supportFragment.resultChecks);
                }
            });
        }
    }
}
