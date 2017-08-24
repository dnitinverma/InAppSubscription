package com.dnitinverma.inappsubscription.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dnitinverma.inappsubscription.R;
import com.dnitinverma.inappsubscription.models.PurchasedItem;

import java.util.ArrayList;


/*
* This Adapter is used for inflate all the already purchased products.
* */
public class PurchasedItemsAdapter extends BaseAdapter{
    Context mContext;
    ArrayList<PurchasedItem> purchasedArrayList;

    public PurchasedItemsAdapter(Context mContext, ArrayList<PurchasedItem> purchasedArrayList) {
        this.mContext = mContext;
        this.purchasedArrayList = purchasedArrayList;
    }

    @Override
    public int getCount() {
        return purchasedArrayList.size();

    }

    @Override
    public PurchasedItem getItem(int position) {
        return purchasedArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_product, parent, false);
            holder.tv_Id = (TextView)convertView.findViewById(R.id.tv_id);
            holder.tv_token = (TextView)convertView.findViewById(R.id.tv_token);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final PurchasedItem model = getItem(position);
        holder.tv_Id.setText("product Id :" + " " +model.getProductId());
        holder.tv_token.setText("product Token :" + " " +model.getPurchaseToken());
        return convertView;
    }

    public static class ViewHolder {
        TextView tv_Id,tv_token;

    }
}
