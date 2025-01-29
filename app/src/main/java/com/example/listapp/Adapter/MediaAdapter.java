package com.example.listapp.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.listapp.Model.MediaItem;
import com.example.listapp.R;
import com.example.listapp.databinding.ItemViewBinding;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private final List<MediaItem> mediaList;
    private OnItemClickListener itemClickListener;
    private OnEditClickListener editClickListener;

    public MediaAdapter(List<MediaItem> mediaList) {
        this.mediaList = mediaList;
    }

    public interface OnEditClickListener {
        void onEditClicked(MediaItem mediaItem, int position);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(MediaItem mediaItem, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemViewBinding binding = ItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem mediaItem = mediaList.get(position);

        holder.binding.itemTitle.setText(mediaItem.getTitle());
        holder.binding.itemDate.setText(mediaItem.getDate());

        Glide.with(holder.binding.itemImage.getContext())
                .load(mediaItem.getImageSource())
                .fitCenter()
                .centerCrop()
                .into(holder.binding.itemImage);


        holder.binding.optionsIcon.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClicked(mediaItem, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(mediaItem, position);
            }
        });

        if (mediaItem.isLocalStorage()) {
            holder.binding.storageIndicator.setImageResource(R.drawable.local_24);
        } else {
            holder.binding.storageIndicator.setImageResource(R.drawable.remote_24);

        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<MediaItem> newData) {
        this.mediaList.clear();
        if (newData != null && !newData.isEmpty()) {
            this.mediaList.addAll(newData);
        }
        notifyDataSetChanged();
    }


    static class MediaViewHolder extends RecyclerView.ViewHolder {

        private final ItemViewBinding binding;

        public MediaViewHolder(@NonNull ItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
