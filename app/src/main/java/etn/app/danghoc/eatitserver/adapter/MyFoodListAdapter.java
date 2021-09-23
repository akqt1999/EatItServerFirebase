package etn.app.danghoc.eatitserver.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.EventBus.CategoryClick;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.callback.IRecyclerClickListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.FoodModel;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.ViewHolder> {
    private Context context;
    private List<FoodModel> foodModelList;


    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_food_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.imgFoodImage);
        holder.txtFoodPrice.setText(new StringBuilder("$")
                .append(foodModelList.get(position).getPrice()));

        holder.txtFoodName.setText(new StringBuilder("")
                .append(foodModelList.get(position).getName()));

        //event
        holder.setListener((view, pos) -> {
            Common.selectedFood = foodModelList.get(pos);
            Common.selectedFood.setKey(pos + "");

        });


    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.txtFoodName)
        TextView txtFoodName;
        @BindView(R.id.txtFoodPrice)
        TextView txtFoodPrice;

        ImageView imgFav;
        @BindView(R.id.imgFoodImage)
        ImageView imgFoodImage;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }

    public FoodModel getItemAtPosition(int pos)
    {
        return foodModelList.get(pos);
    }
}
