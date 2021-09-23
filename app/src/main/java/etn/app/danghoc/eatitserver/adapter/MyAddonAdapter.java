package etn.app.danghoc.eatitserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.EventBus.SelectAddonModel;
import etn.app.danghoc.eatitserver.EventBus.SelectSizeModel;
import etn.app.danghoc.eatitserver.EventBus.UpdateAddonModel;
import etn.app.danghoc.eatitserver.EventBus.UpdateSizeModel;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.callback.IRecyclerClickListener;
import etn.app.danghoc.eatitserver.model.AddonModel;
import etn.app.danghoc.eatitserver.model.SizeModel;

public class MyAddonAdapter  extends RecyclerView.Adapter<MyAddonAdapter.MyViewHolder> {

    Context context;
    List<AddonModel> addonModels;

    UpdateAddonModel updateAddonModel;
    int edtPos;

    public MyAddonAdapter(Context context, List<AddonModel> addonModels) {
        this.context = context;
        this.addonModels = addonModels;
        edtPos=-1;
        updateAddonModel=new UpdateAddonModel();
    }

    @NonNull
    @Override
    public MyAddonAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyAddonAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_size_addon_display, parent, false));


    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name.setText(addonModels.get(position).getName());
        holder.txt_price.setText(addonModels.get(position).getPrice() + "");

        //event
        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete item
                addonModels.remove(position);
                notifyItemRemoved(position);
                updateAddonModel.setAddonModel(addonModels);//set for event
                EventBus.getDefault().postSticky(updateAddonModel); // send event

            }
        });

        holder.setListener((view, pos) -> {
            edtPos=position;
            EventBus.getDefault().postSticky(new SelectAddonModel(addonModels.get(pos)));
        });
    }


    @Override
    public int getItemCount() {
        return addonModels.size();
    }

    public void addNewSize(AddonModel addonModel) {
        addonModels.add(addonModel);
        notifyItemInserted(addonModels.size()-1);
        updateAddonModel.setAddonModel(addonModels);
        EventBus.getDefault().postSticky(updateAddonModel);

    }

    public void editSize(AddonModel addonModel) {
        if(edtPos!=-1)
        {
            addonModels.set(edtPos,addonModel);
            notifyItemInserted(edtPos);
            edtPos=-1;//request variable after success
            //send update
            updateAddonModel.setAddonModel(addonModels);
            EventBus.getDefault().postSticky(updateAddonModel);

        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_price)
        TextView txt_price;
        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> listener.onItemClickListener(v, getAdapterPosition()));
        }
    }
}

