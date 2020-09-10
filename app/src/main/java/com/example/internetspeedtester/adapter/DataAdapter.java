package com.example.internetspeedtester.adapter;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internetspeedtester.R;
import com.example.internetspeedtester.utils.DataInfo;

import java.util.List;
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder>{


    //   public List<Datainfo> itemdata;
  //  ItemsClicked activity;
    public List<DataInfo> itemdata;
   private FragmentActivity activity;


    public interface ItemsClicked{
        void onItemSelected(DataInfo index);
    }


 /*   public DataAdapter(Context context, List<Datainfo>itemdata){
        this.itemdata=itemdata;
        activity=(ItemsClicked)context;
    }

  */

    public DataAdapter(Activity activity, List<DataInfo> itemdata) {
        this.activity = (FragmentActivity) activity;
        this.itemdata = itemdata;
    }


    @NonNull
    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.item_list,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.itemView.setTag(itemdata.get(position));
        DataInfo di =itemdata .get(position);
        holder.date.setText(di.date);
        holder.wifi.setText(di.wifi);
        holder.mobile.setText(di.mobile);
        holder.ttl.setText(di.total);
        if ((position)% 2 == 0) {
            holder.linearLayout.setBackgroundColor(ContextCompat.getColor(this.activity, R.color.down));
        } else {
            holder.linearLayout.setBackgroundColor(ContextCompat.getColor(this.activity, R.color.up));
        }
    }


    public void updateData(List<DataInfo> temp) {
        this.itemdata = temp;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount()
    {
        return itemdata.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView date,mobile,wifi,ttl;
        LinearLayout linearLayout;
        public ViewHolder(@NonNull final View itemView) {

            super(itemView);

            date=itemView.findViewById(R.id.id_date);
            mobile=itemView.findViewById(R.id.mobile);
            wifi=itemView.findViewById(R.id.id_wifi);
            ttl=itemView.findViewById(R.id.total);
            linearLayout=itemView.findViewById(R.id.ll);


        }
    }
}
