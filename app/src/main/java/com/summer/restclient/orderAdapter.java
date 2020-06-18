package com.summer.restclient;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class orderAdapter extends RecyclerView.Adapter<orderAdapter.orderViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private OnItemClickListener clickListener;

    public orderAdapter(Context context,Cursor cursor){
        this.mCursor=cursor;
        this.mContext=context;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void changeCursor(Cursor cursor){
        if(mCursor!=null)mCursor.close();
        mCursor=cursor;
        if(cursor!=null){
            this.notifyDataSetChanged();
        }
    }
    @NonNull
    @Override
    public orderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_list,parent,false);
        return new orderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final orderViewHolder holder, final int position) {
        if(!mCursor.moveToPosition(position)){return;}
        long ID=mCursor.getLong(mCursor.getColumnIndex("_ID"));
        String address=mCursor.getString(mCursor.getColumnIndex("address"));

        holder.tvOrderID.setText(String.valueOf(ID));
        holder.tvAddress.setText(address);
        holder.itemView.setTag(ID);


        if(clickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(holder.itemView,position);
                }
            });
        }

    }

    class orderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderID;
        TextView tvAddress;
        public orderViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvOrderID=(TextView)itemView.findViewById(R.id.tvOrderID);
            tvAddress=(TextView)itemView.findViewById(R.id.tvAddress);

        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.clickListener=listener;
    }

    public interface  OnItemClickListener{
        void onItemClick(View view,int position);
    }
}
