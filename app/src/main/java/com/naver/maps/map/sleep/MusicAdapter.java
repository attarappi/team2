package com.naver.maps.map.sleep;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private final List<String> musicList;
    private final OnItemClickListener listener;
    private int selectedPosition = -1; // 선택된 항목의 인덱스

    public interface OnItemClickListener {
        void onItemClick(String fileName, int position);
        void onDelete(String fileName, int position);
    }

    public MusicAdapter(List<String> musicList, OnItemClickListener listener) {
        this.musicList = musicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fileName = musicList.get(position);
        holder.tvMusicName.setText(fileName);

        // 선택된 항목 강조
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // 선택된 항목의 배경 색상 변경
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // 기본 배경 색상
        }

        // 항목 클릭 시
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position; // 선택된 위치 업데이트

            notifyItemChanged(previousPosition); // 이전 선택된 항목 초기화
            notifyItemChanged(selectedPosition); // 새로 선택된 항목 강조

            listener.onItemClick(fileName, position);
        });

        // 삭제 버튼 클릭 시
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(fileName, position));
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvMusicName;
        public final Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMusicName = itemView.findViewById(R.id.tvMusicName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
