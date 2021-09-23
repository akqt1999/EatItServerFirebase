package etn.app.danghoc.eatitserver.ui.send_news;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import etn.app.danghoc.eatitserver.R;

public class SendNewFragment extends Fragment {

    private SendNewViewModel mViewModel;

    public static SendNewFragment newInstance() {
        return new SendNewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_new_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SendNewViewModel.class);
        // TODO: Use the ViewModel
    }

}