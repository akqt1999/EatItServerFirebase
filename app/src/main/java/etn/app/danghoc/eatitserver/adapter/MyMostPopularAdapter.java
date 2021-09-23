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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.callback.IRecyclerClickListener;
import etn.app.danghoc.eatitserver.model.MostPopularModel;

public class MyMostPopularAdapter extends RecyclerView.Adapter<MyMostPopularAdapter.MyViewHolder> {

    Context context;
    List<MostPopularModel>mostPopularModelList;

    public MyMostPopularAdapter(Context context, List<MostPopularModel> mostPopularModelList) {
        this.context = context;
        this.mostPopularModelList = mostPopularModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(mostPopularModelList.get(position).getImage()).into(holder.imgCategory);
        holder.txtCategory.setText(new StringBuilder(mostPopularModelList.get(position).getName()));

        holder.setListener((view, pos) -> {
        });
    }

    @Override
    public int getItemCount() {
        return mostPopularModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        Unbinder unbinder;
        @BindView(R.id.imgCategory)
        ImageView imgCategory;
        @BindView(R.id.txtCategory)
        TextView txtCategory;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
/*
sao con chan khong co dong luc vay ong co oi , tri oi m dung lai di m dang suy nghi cai gi vay ha tri
 */