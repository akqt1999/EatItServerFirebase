package etn.app.danghoc.eatitserver.ui.most_popular;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.adapter.MyBestDealsAdapter;
import etn.app.danghoc.eatitserver.adapter.MyMostPopularAdapter;
import etn.app.danghoc.eatitserver.model.BestDealModel;
import etn.app.danghoc.eatitserver.model.MostPopularModel;
import etn.app.danghoc.eatitserver.ui.best_deals.BestDealsViewModel;

public class MostPopularFragment extends Fragment {

    private MostPopularViewModel mViewModel;

    //cai chung
    Unbinder unbinder;
    @BindView(R.id.recycler_most_popular)
    RecyclerView recycler_most_popular;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMostPopularAdapter adapter;

    List<MostPopularModel> mostPopularModelList1;
    //----------------------------------


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel =
                new ViewModelProvider(this).get(MostPopularViewModel.class);

        View root = inflater.inflate(R.layout.most_popular_fragment, container, false);

        unbinder = ButterKnife.bind(this, root);

        init();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            //
            Toast.makeText(getContext(), s + "", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getMostPopularListMutable().observe(getViewLifecycleOwner(), mostPopularModelList -> {
            dialog.dismiss();
            mostPopularModelList1 = mostPopularModelList;
            adapter = new MyMostPopularAdapter(getActivity(),mostPopularModelList1);
            recycler_most_popular.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //          recyclerViewMenu.setLayoutAnimation(layoutAnimationController);
        });


        return root;


    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();
        //    layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_most_popular.setLayoutManager(layoutManager);
        recycler_most_popular.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }


}