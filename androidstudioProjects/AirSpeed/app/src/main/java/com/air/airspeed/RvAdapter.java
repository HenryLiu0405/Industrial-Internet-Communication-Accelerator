package com.air.airspeed;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lius on 2018/9/1.
 */



public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {
    public interface OnItemClickListener{
        void onClick(View view, int position);
    }

    private Context mContext;
    private List<ResolveInfo> mlsResolveInfo;
    private OnItemClickListener mListener;

    public RvAdapter(Context context, List<ResolveInfo> resolveInfos, OnItemClickListener listener) {
        this.mContext = context;
        this.mlsResolveInfo = resolveInfos;
        this.mListener = listener;

    }

    public void setmListener(OnItemClickListener listener)  {
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)  {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item, parent,false);
        RvAdapter.ViewHolder viewHolder = new RvAdapter.ViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)  {
        ResolveInfo resolveInfo = mlsResolveInfo.get(position);
        holder.imageView.setImageDrawable(resolveInfo.activityInfo.loadIcon(mContext.getPackageManager()));
        holder.textView.setText(resolveInfo.activityInfo.applicationInfo.loadLabel(mContext.getPackageManager()));
    }

    @Override
    public int getItemCount()  {
        return mlsResolveInfo.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        OnItemClickListener onItemClickListener;
        ImageView imageView;
        TextView textView;

        public ViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener = onItemClickListener;
            imageView = (ImageView) itemView.findViewById(R.id.iv_icon);
            textView = (TextView)itemView.findViewById(R.id.tv_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)  {
            if (onItemClickListener != null) {
                onItemClickListener.onClick(v, getAdapterPosition());
            }
        }
    }

}
