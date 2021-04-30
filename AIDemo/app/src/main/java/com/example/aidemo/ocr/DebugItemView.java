package com.example.aidemo.ocr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidemo.R;
import com.example.aidemo.databinding.RvDebugViewItemBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DebugItemView extends RecyclerView.Adapter<DebugItemView.MyViewHolder> {


    ArrayList<TextBlock> textBlocks = new ArrayList<TextBlock>();

    public void setTextBlocks(ArrayList<TextBlock> textBlocks) {
        this.textBlocks.clear();
        this.textBlocks.addAll(textBlocks);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//        View itemView = layoutInflater.inflate(R.layout.rv_debug_view_item,parent,false);
        RvDebugViewItemBinding binding = DataBindingUtil.inflate(layoutInflater,R.layout.rv_debug_view_item,parent,false);
        return new MyViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RvDebugViewItemBinding binding = DataBindingUtil.getBinding(holder.itemView);

        TextBlock textBlock = textBlocks.get(position);
        String boxPointStr = textBlock.boxPoint.stream().parallel().map(
                point -> String.format("[%d, %d]", point.x, point.y))
                .collect(Collectors.joining(","));

        float[] charScore = textBlock.charScore;
        List<String> strList = new LinkedList<>();
        for(float i : charScore) {
            strList.add(String.format("%.2f", i));
        }
        String charScoreStr = String.join(",", strList);

        binding.blockIndexTv.setText(String.valueOf(position + 1));
        binding.include.boxPointTv.setText(boxPointStr);
        binding.include.boxScoreTv.setText(String.valueOf(textBlock.boxScore));
        binding.include.angleIndexTv.setText(String.valueOf(textBlock.angleIndex));
        binding.include.angleScoreTv.setText(String.format("%.2f ",textBlock.angleScore));
        binding.include.angleTimeTv.setText(String.format("%.2f ms",textBlock.angleTime));
        binding.include.textTv.setText(textBlock.text);
        binding.include.charScoresTv.setText(charScoreStr);
        binding.include.crnnTimeTv.setText(String.format("%.2f ms",textBlock.crnnTime));
        binding.include.blockTimeTv.setText(String.format("%.2f ms",textBlock.blockTime));
    }

    @Override
    public int getItemCount() {
        return textBlocks.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
