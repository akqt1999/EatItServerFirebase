package etn.app.danghoc.eatitserver.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import etn.app.danghoc.eatitserver.EventBus.LoadOrderEvent;
import etn.app.danghoc.eatitserver.R;

public class BottomSheetOrderFragment extends BottomSheetDialogFragment {
    private static BottomSheetOrderFragment instance;
    private Unbinder unbinder;

    @OnClick(R.id.place_filter)
    public void onPlaceFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(0));
        dismiss();
    }

    @OnClick(R.id.shipped_filter)
    public void onShippedFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(2));
        dismiss();
    }
    @OnClick(R.id.shipping_filter)
    public void onShippingFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(1));
        dismiss();
    }
    @OnClick(R.id.cancelled_filter)
    public void onCancelledFilterClick(){
        EventBus.getDefault().postSticky(new LoadOrderEvent(-1));
        dismiss();
    }

    public static BottomSheetOrderFragment getInstance(){
        return instance==null?new BottomSheetOrderFragment():instance;
    }

    public BottomSheetOrderFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView=inflater.inflate(R.layout.fragment_order_filter,container,false);
        unbinder= ButterKnife.bind(this,itemView);
        return itemView;
    }
}
