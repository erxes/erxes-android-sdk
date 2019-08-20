package com.newmedia.erxeslibrary.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.DraweeView;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.model.FileAttachment;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter {

    private final int imageItem = 0;
    private final int fileItem = 1;
    private GenericDraweeHierarchyBuilder builder;
    private Activity activity;
    private List<FileAttachment> fileAttachments;
    private Config config;

    FileAdapter(Activity activity, List<FileAttachment> fileAttachments) {
        this.activity = activity;
        this.fileAttachments = fileAttachments;
        this.config = Config.getInstance(activity);

        Drawable failureDrawable = activity.getResources().getDrawable(R.drawable.ic_error_black_96dp);
        DrawableCompat.setTint(failureDrawable, Color.RED);

        ProgressBarDrawable progressBarDrawable = new ProgressBarDrawable();
        progressBarDrawable.setColor(config.colorCode);
        progressBarDrawable.setBackgroundColor(config.getInColor(config.colorCode));

        progressBarDrawable
                .setRadius(activity.getResources().getDimensionPixelSize(R.dimen.drawee_hierarchy_progress_radius));

        builder = new GenericDraweeHierarchyBuilder(activity.getResources());
        builder.setPlaceholderImage(config.getImageVIcon(activity,config.colorCode));
        builder.setProgressBarImage(progressBarDrawable);
        builder.setFailureImage(failureDrawable, ScalingUtils.ScaleType.CENTER_INSIDE);

        int color = activity.getResources().getColor(R.color.black_10);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(16);
        roundingParams.setBorder(color,1);
        builder.setRoundingParams(roundingParams);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder vh;
        View v;
        if (i == imageItem) {
            v = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.item_image_file, viewGroup, false);
            vh = new ImageViewHolder(v);
        } else {
            v = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.item_other_file, viewGroup, false);
            vh = new OtherViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ImageViewHolder) {
            ((ImageViewHolder) viewHolder).bind(fileAttachments.get(i), i);
        } else {
            ((OtherViewHolder) viewHolder).bind(fileAttachments.get(i), i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Log.e(getClass().getName(), "getItemViewType: " + fileAttachments.get(position).getUrl() );
        if (fileAttachments.get(position).getType().contains("image") &&
                !fileAttachments.get(position).getType().contains("image/svg")) {
            return imageItem;
        } else return fileItem;
    }

    @Override
    public int getItemCount() {
        return fileAttachments != null ? fileAttachments.size() : 0;
    }

    class OtherViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        OtherViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.fileImage);
        }

        void bind(final FileAttachment fileAttachment, int position) {
            if (fileAttachment.getType().contains("application/pdf")) {
                imageView.setImageResource(R.drawable.filepdf);
            } else if (fileAttachment.getType().contains("application") && fileAttachment.getType().contains("word")){
                imageView.setImageResource(R.drawable.fileword);
            } else {
                imageView.setImageResource(R.drawable.file);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fileAttachment.getUrl().startsWith("http")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileAttachment.getUrl()));
                        activity.startActivity(browserIntent);
                    } else {
                        Toast.makeText(activity, "Invalid file",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        private DraweeView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image);
        }

        void bind(FileAttachment fileAttachment, final int position) {
            Uri uri = Uri.parse(fileAttachment.getUrl());
            imageView.setHierarchy(builder.build());
            imageView.setImageURI(uri);

            final List<FileAttachment> fileAttachmentList = new ArrayList<>();
            for (FileAttachment attachment : fileAttachments) {
                if (attachment.getType().contains("image") &&
                        !attachment.getType().contains("image/svg")) {
                    fileAttachmentList.add(attachment);
                }
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ImageViewer.Builder(activity, fileAttachmentList)
                            .setFormatter(new ImageViewer.Formatter<FileAttachment>() {
                                @Override
                                public String format(FileAttachment fileAttachment1) {
                                    return fileAttachment1.getUrl();
                                }
                            })
                            .setStartPosition(position)
                            .setBackgroundColor(config.colorCode)
                            .setImageMarginPx(20)
                            .allowZooming(true)
                            .allowSwipeToDismiss(true)
                            .show();
                }
            });
        }
    }
}
