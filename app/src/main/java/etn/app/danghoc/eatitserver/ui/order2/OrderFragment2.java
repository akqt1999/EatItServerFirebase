package etn.app.danghoc.eatitserver.ui.order2;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import etn.app.danghoc.eatitserver.R;

public class OrderFragment2 extends Fragment {

    private OrderFragment2ViewModel mViewModel;

    public static OrderFragment2 newInstance() {
        return new OrderFragment2();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(OrderFragment2ViewModel.class);
        // TODO: Use the ViewModel
    }

}