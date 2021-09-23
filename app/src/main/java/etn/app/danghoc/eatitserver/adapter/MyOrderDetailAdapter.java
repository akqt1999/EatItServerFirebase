package etn.app.danghoc.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.model.AddonModel;
import etn.app.danghoc.eatitserver.model.CartItem;
import etn.app.danghoc.eatitserver.model.SizeModel;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder>{

    Context context;
    List<CartItem>cartItemList;
    Gson gson;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        gson=new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_order_detail_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(cartItemList.get(position).getFoodImage())
                .into(holder.img_food_image);
        holder.txt_food_quatity.setText(new StringBuilder("Quantity")
                .append(cartItemList.get(position).getFoodQuantity()));

        holder.txt_food_name.setText(cartItemList.get(position).getFoodName());

        SizeModel sizeModel=gson.fromJson(cartItemList.get(position).getFoodSize(),new TypeToken<SizeModel>(){}.getType());// cai nay do la luc luu voi file gson doc giai ma file gson
        if(sizeModel!=null)
            holder.txt_food_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
        if(!cartItemList.get(position).getFoodAddon().equals("Default"))
        {
                List<AddonModel>addonModelList=gson.fromJson(cartItemList.get(position).getFoodAddon(),new TypeToken<AddonModel>(){}.getType());
                StringBuilder addonString=new StringBuilder();
                if(addonModelList!=null)
                {
                    for (AddonModel addonModel:addonModelList)
                        addonString.append(addonModel.getName()).append(",");
                    addonString.delete(addonString.length()-1,addonString.length());
                    holder.txt_food_add_on.setText(new StringBuilder("Addon: ").append(addonString));
                }


        }
        else
        {
           holder.txt_food_add_on.setText(new StringBuilder("Addon: Default"));
        }

    }


    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_add_on)
        TextView txt_food_add_on;
        @BindView(R.id.txt_food_quatity)
        TextView txt_food_quatity;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);
        }
    }
}
