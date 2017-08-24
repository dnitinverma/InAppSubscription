package inappsubscription.dnitinverma.inappsubscription.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.digivive.inappsubscriptiondemo.R;
import com.digivive.inappsubscriptiondemo.activities.BuyNewItemActivity;
import inappsubscription.dnitinverma.inappsubscription.models.Product;

import java.util.ArrayList;

/*
* This Adapter is used for inflate all the new available products.
* */
    public class BuyItemsAdapter extends BaseAdapter{
    Context mContext;
    ArrayList<Product> productArrayList;

    public BuyItemsAdapter(Context mContext, ArrayList<Product> productArrayList) {
        this.mContext = mContext;
        this.productArrayList = productArrayList;
    }

    @Override
    public int getCount() {
        return productArrayList.size();

    }

    @Override
    public Product getItem(int position) {
        return productArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_buy_item, parent, false);
            holder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);
            holder.tv_description = (TextView)convertView.findViewById(R.id.tv_description);
            holder.tv_price = (TextView)convertView.findViewById(R.id.tv_price);
            holder.radioButton = (RadioButton)convertView.findViewById(R.id.rb_payment);
            holder.lin_payment = (LinearLayout)convertView.findViewById(R.id.lin_payment);
            holder.lin_payment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BuyNewItemActivity)mContext).buyItem(productArrayList.get(position),position);
                    for(int i=0;i<productArrayList.size();i++)
                    {
                        if(i==position) {
                            productArrayList.get(i).isSelect = true;
                            holder.radioButton.setChecked(true);
                        }
                        else {
                            productArrayList.get(i).isSelect = false;
                            holder.radioButton.setChecked(false);
                        }
                    }
                    notifyDataSetChanged();
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Product model = getItem(position);
        if(productArrayList.get(position).isSelect == true) {
            model.isSelect = true;
            holder.radioButton.setChecked(true);
        } else {
            model.isSelect = false;
            holder.radioButton.setChecked(false);
        }
        holder.tv_title.setText(model.getTitle());
        holder.tv_description.setText(model.getDescription());
        holder.tv_price.setText(model.getPrice());
        return convertView;
    }

    public static class ViewHolder {
        TextView tv_title,tv_description,tv_price;
        public RadioButton radioButton;
        public LinearLayout lin_payment;
    }
}
