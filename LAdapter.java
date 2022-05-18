package com.example.foredownload1.adpter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foredownload1.R;
import com.example.foredownload1.models.ProgessModle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LAdapter extends RecyclerView.Adapter<LAdapter.ViewHolder> {
    List<ProgessModle> applist = new ArrayList<>();
    Map<Integer,ProgressBar> progresslist = new HashMap<>();
    private int id = 0;
    //Map<String,Integer>  tag = new LinkedHashMap<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(applist.get(position).getAppname());
       // holder.progress.setText("progress"+applist.get(position).getProgress());
       // holder.itemView.setTag(position);
        progresslist.put(position,holder.downlownBar);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove((Integer) v.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return applist.size();
    }

    public void  setApplist(List<String> appnlist) {
        this.applist = applist;
    }
    /*
    public void setProgress(int progress, String appname){
        Log.d("ww", "setProgress: "+ appname);
        for(ProgessModle progessModle:applist){
            Log.d("ww", "setProgress: "+progessModle.getAppname());

            if(progessModle.getAppname().equals(appname)){
                progessModle.setProgress(progress);
                System.out.println(appname+progress);
                notifyItemChanged(progessModle.getPosition());
                break;
            }
        }
    }

     */
    public void setProgrss(int progrss,int id){
        System.out.println("id" + id);
        ProgressBar progressBar = progresslist.get(id-1);
        progressBar.setProgress(progrss);

    }

    public void add(String appname) {
        applist.add(new ProgessModle(0,appname,id));
        id++;
        System.out.println(applist.get(id-1).appname);
        notifyDataSetChanged();
    }
    public void remove(Integer id){
        applist.remove(id);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        TextView progress;
        ProgressBar downlownBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.appname);
           // progress = itemView.findViewById(R.id.appprogress);
            downlownBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
