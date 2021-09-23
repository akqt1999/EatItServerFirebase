package etn.app.danghoc.eatitserver.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.callback.IRecyclerClickListener;
import etn.app.danghoc.eatitserver.model.ShipperModel;

public class MyShipperSelectionAdapter extends RecyclerView.Adapter<MyShipperSelectionAdapter.ViewHolder> {

    private Context context;
    List<ShipperModel>shipperModelList;
    private ImageView lastCheckedImageView=null;
    private ShipperModel selectShipper=null;

    public MyShipperSelectionAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }

    /*
            vui khong duoc bao lau thi con lai buon , co le day la het co roi:(I(((((
            khong ai tin con het nhung co khong sao , con khong buon dau , con song vi minh chu con khong ssong vi nguoi khac :))
             */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView= LayoutInflater.from(context).inflate(R.layout.layout_shipper_select,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.txt_name.setText(new StringBuilder(shipperModelList.get(position).getName()));
            holder.txt_phone.setText(new StringBuilder(shipperModelList.get(position).getPhone()));

            holder.setiRecyclerClickListener(new IRecyclerClickListener() {
                @Override
                public void onItemClickListener(View view, int pos) {
                    if(lastCheckedImageView!=null)
                        lastCheckedImageView.setImageResource(0);
                    holder.img_checked.setImageResource(R.drawable.ic_baseline_check_24);
                    lastCheckedImageView=holder.img_checked; //???
                    selectShipper=shipperModelList.get(pos);

                }
            });
    }

    public ShipperModel getSelectShipper() {
        return selectShipper;
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Unbinder unbinder;

        @BindView(R.id.txt_phone)
        TextView txt_phone;
        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.img_checked)
        ImageView img_checked;


        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener){
            this.iRecyclerClickListener=iRecyclerClickListener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerClickListener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
