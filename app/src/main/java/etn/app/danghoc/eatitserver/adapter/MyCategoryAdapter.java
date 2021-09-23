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

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.EventBus.CategoryClick;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.callback.IRecyclerClickListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.CategoryModel;

public class MyCategoryAdapter extends RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder> {
    Context context;
    List<CategoryModel> categoryModelList;


    public MyCategoryAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(categoryModelList.get(position).getImage()).into(holder.imgCategory);
        holder.txtCategory.setText(new StringBuilder(categoryModelList.get(position).getName()));

        //event (lan dau tien thay)
        holder.setListener((view, pos) -> {
            Common.categorySelected=categoryModelList.get(pos);
            EventBus.getDefault().postSticky(new CategoryClick(true,categoryModelList.get(pos)) );
        });
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {
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

    @Override
    public int getItemViewType(int position) {

        if (categoryModelList.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;
        else {
            if (categoryModelList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == categoryModelList.size() - 1) ? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
        }

    }
    
}
