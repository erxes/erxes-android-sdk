package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

import java.util.Arrays;
import java.util.List;

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.ViewHolder> {

    private List<String> options;
    private SupportFragment supportFragment;
    private int parentPosition;
    private RecyclerView checkList;

    public CheckAdapter(SupportFragment supportFragment, List<String> options, int parentPosition, RecyclerView checkList) {
        this.checkList = checkList;
        this.supportFragment = supportFragment;
        this.options = options;
        this.parentPosition = parentPosition;
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
            ColorStateList  colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, // unchecked
                            new int[]{android.R.attr.state_checked} , // checked
                    },
                    new int[]{
                            Color.parseColor("#000000"),
                            ((ConversationListActivity) supportFragment.getActivity()).config.colorCode,
                    }
            );

            CompoundButtonCompat.setButtonTintList(checkBox,colorStateList);
            checkBox.setText(option);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    supportFragment.setCheckValue(parentPosition);
                    checkList.setBackgroundResource(R.drawable.rounded_input);
                }
            });
        }
    }
}
