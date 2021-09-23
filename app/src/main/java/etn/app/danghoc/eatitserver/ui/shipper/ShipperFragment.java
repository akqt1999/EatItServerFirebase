package etn.app.danghoc.eatitserver.ui.shipper;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eatitserver.EventBus.ChangeMenuClick;
import etn.app.danghoc.eatitserver.EventBus.UpdateShipperEvent;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.adapter.MyShipperAdapter;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.FoodModel;
import etn.app.danghoc.eatitserver.model.ShipperModel;

public class ShipperFragment extends Fragment {

    private ShipperViewModel mViewModel;


    private Unbinder unbinder;

    @BindView(R.id.recycler_shipper)
    RecyclerView recycler_shipper;
    AlertDialog dialog;
    MyShipperAdapter adapter;
    List<ShipperModel> shipperModelList,saveShipperBeforeSearchList;




    public static ShipperFragment newInstance() {
        return new ShipperFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_shipper, container, false);
        mViewModel = new ViewModelProvider(this).get(ShipperViewModel.class);

        unbinder = ButterKnife.bind(this, itemView);
        initView();
        mViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getShipperMutableList().observe(this, shipperModels -> {
            shipperModelList = shipperModels;

            if(saveShipperBeforeSearchList==null)
            saveShipperBeforeSearchList=shipperModels;

            Log.d("abss","ban dau "+saveShipperBeforeSearchList.size()+"");
            dialog.dismiss();
            adapter = new MyShipperAdapter(getContext(), shipperModelList);
            recycler_shipper.setAdapter(adapter);
        });

        return itemView;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu,menu);

        MenuItem menuItem=menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSeachFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);

        // close seach tro ve ban cu
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed = searchView.findViewById(R.id.search_src_text);
                //clear text
                ed.setText("");
                //clear query
                searchView.setQuery("", false);
                // the search widget
                menuItem.collapseActionView();
                //restore result to original

                //if(saveShipperBeforeSearchList!=null)
                mViewModel.getShipperMutableList().setValue(saveShipperBeforeSearchList);
                    Log.d("abss",saveShipperBeforeSearchList.size()+"");

            }
        });

    }

    private void startSeachFood(String query) {
        List<ShipperModel>resultShipper=new ArrayList<>();
        for(ShipperModel shipperModel:shipperModelList)
        {
            if(shipperModel.getPhone().toLowerCase().contains(query.toLowerCase())
                    ||shipperModel.getName().toLowerCase().contains(query.toLowerCase())) // ten va sdt
            {
                resultShipper.add(shipperModel);
            }
        }

        mViewModel.getShipperMutableList().setValue(resultShipper);
    }



    private void initView() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_shipper.setLayoutManager(layoutManager);


    }


    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateShipperEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateShipperEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateShipperActive(UpdateShipperEvent event) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("active", event.isActive());// lay du lieu button swich
        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPER)
                .child(event.getShipperModel().getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Update stato to " + event.isActive(), Toast.LENGTH_SHORT).show());
        Log.d("keyy",event.getShipperModel().getKey()+"");

    }


}
/*
con da qua di bvao so luong ma con khong di vao chat lyong

 */