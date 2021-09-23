package etn.app.danghoc.eatitserver.ui.order;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
import etn.app.danghoc.eatitserver.EventBus.ChangeMenuClick;
import etn.app.danghoc.eatitserver.EventBus.LoadOrderEvent;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.Remote.IFCMService;
import etn.app.danghoc.eatitserver.Remote.RetrofitFCMClient;
import etn.app.danghoc.eatitserver.TrackingOrderActivity;
import etn.app.danghoc.eatitserver.adapter.MyOrderAdapter;
import etn.app.danghoc.eatitserver.adapter.MyShipperSelectionAdapter;
import etn.app.danghoc.eatitserver.callback.IShipperLocalCallbackListener;
import etn.app.danghoc.eatitserver.common.BottomSheetOrderFragment;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.common.MySwiperHelper;
import etn.app.danghoc.eatitserver.model.FCMResponse;
import etn.app.danghoc.eatitserver.model.FCMSendData;
import etn.app.danghoc.eatitserver.model.OrderModel;
import etn.app.danghoc.eatitserver.model.ShipperModel;
import etn.app.danghoc.eatitserver.model.ShippingOrderModel;
import etn.app.danghoc.eatitserver.model.TokenModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends Fragment implements IShipperLocalCallbackListener {

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;

    RecyclerView recyclerView_shipper;

    Unbinder unbinder;

    // send notification
    IFCMService ifcmService;


    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private OrderViewModel orderViewModel;
    private MyOrderAdapter adapter;
    private MyShipperSelectionAdapter myShipperSelectAdapter;

    private IShipperLocalCallbackListener shipperLocalCallbackListener;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        orderViewModel.getMessageError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(), "" + s, Toast.LENGTH_SHORT).show();
            }
        });
        orderViewModel.getOrderModelMutableLiveData().observe(this, orderModelList -> {
            Log.d("avvv", "" + orderModelList.size());
            if (orderModelList != null) {
                Log.d("avvv", "" + orderModelList.size() + "khac null");
                adapter = new MyOrderAdapter(getContext(), orderModelList);
                recycler_order.setAdapter(adapter);

                updateTextCounter();
            }
        });

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        unbinder = ButterKnife.bind(this, root);
        initView();
        return root;
    }

    private void initView() {

        shipperLocalCallbackListener = this;


        setHasOptionsMenu(true);
        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_order, width / 6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Directions", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {

                    OrderModel orderModel=((MyOrderAdapter)recycler_order.getAdapter()).getItemAtPosition(pos);
                    if(orderModel.getOrderStatus()==1)//shipping
                    {
                            Common.currentOrderSelected=orderModel;
                            startActivity(new Intent(getContext(), TrackingOrderActivity.class));
                    }
                    else
                    {
                        Toast.makeText(getContext(),""+new StringBuilder("Your order is")
                                .append(Common.convertStatusToString(orderModel.getOrderStatus()))
                                .append(" so  can't track direction"), Toast.LENGTH_SHORT).show();
                    }

                        }));
                buf.add(new MyButton(getContext(), "Call", 30, 0, Color.parseColor("#560027"),
                        pos -> {
                            Dexter.withContext(getContext())
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                            OrderModel orderModel = adapter.getItemAtPosition(pos);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel: ")
                                                    .append(orderModel.getUserPhone()).toString()));
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                            Toast.makeText(getContext(), "You must accept " + permissionDeniedResponse.getPermissionName(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                        }
                                    }).check();


                        }));

                buf.add(new MyButton(getContext(), "Remove", 30, 0, Color.parseColor("#12005e"),
                        pos -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Do you really want to delete this order? ")
                                    .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        OrderModel orderModel = adapter.getItemAtPosition(pos);
                                        FirebaseDatabase.getInstance()
                                                .getReference(Common.ORDER_REF)
                                                .child(orderModel.getKey())
                                                .removeValue()
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        adapter.removeItem(pos);
                                                        adapter.notifyItemRemoved(pos);
                                                        updateTextCounter();
                                                        Toast.makeText(getContext(), "Order has been delete", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                });


                                    });
                            //create dialog
                            AlertDialog dialog1 = builder.create();
                            dialog1.show();

                            Button negativeButton = dialog1.getButton(DialogInterface.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);
                            Button positiveButton = dialog1.getButton(DialogInterface.BUTTON_POSITIVE);
                            positiveButton.setTextColor(Color.RED);

                        }));

                buf.add(new MyButton(getContext(), "Edit", 30, 0, Color.parseColor("#336699"),
                        pos -> {

                            showEditDialog(adapter.getItemAtPosition(pos), pos);

                        }));


            }
        };

    }

    private void showEditDialog(OrderModel orderModel, int pos) {
        View layout_dialog;
        AlertDialog.Builder builder;
        if (orderModel.getOrderStatus() == 0) //shipping
        {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipping, null);

            recyclerView_shipper = layout_dialog.findViewById(R.id.recycler_shippers);

            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                    .setView(layout_dialog);
        } else if (orderModel.getOrderStatus() == -1)//cancel
        {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_cancelled, null);
            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
        } else //shipped
        {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipped, null);
            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
        }


        //view
        Button btn_ok = layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_shipping = layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_cancelled = layout_dialog.findViewById(R.id.rdi_cancelled);
        RadioButton rdi_delete = layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_placed = layout_dialog.findViewById(R.id.rdi_restore_placed);
        RadioButton rdi_shipped = layout_dialog.findViewById(R.id.rdi_shipped);

        TextView txt_status = layout_dialog.findViewById(R.id.txt_status);

        //set Data
        txt_status.setText(new StringBuilder("Order Status(")
                .append(Common.convertStatusToString(orderModel.getOrderStatus())));

        //create dialog
        AlertDialog dialog = builder.create();
        dialog.show();


        if (orderModel.getOrderStatus() == 0)//shipping
            loadShipperList(pos, orderModel, dialog, btn_ok, btn_cancel, rdi_shipping, rdi_shipped, rdi_cancelled,
                    rdi_delete, rdi_restore_placed);
        else
            showDialog(pos, orderModel, dialog, btn_ok, btn_cancel, rdi_shipping, rdi_shipped, rdi_cancelled,
                    rdi_delete, rdi_restore_placed);


    }

    private void loadShipperList(int pos, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {

        List<ShipperModel> tempList = new ArrayList<>();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER);
        Query shipperActive = shipperRef.orderByChild("active").equalTo(true);

        shipperActive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot shipperSnapshot : snapshot.getChildren()) {
                    ShipperModel shipperModel = shipperSnapshot.getValue(ShipperModel.class);
                    shipperModel.setKey(shipperSnapshot.getKey());
                    tempList.add(shipperModel);
                }
                shipperLocalCallbackListener.onShipperLoadSuccess12(pos, orderModel, tempList,
                        dialog, btn_ok, btn_cancel,
                        rdi_shipping, rdi_shipped, rdi_cancelled, rdi_delete, rdi_restore_placed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                shipperLocalCallbackListener.onShipperLoadFailed(error.getMessage());
            }
        });

        /*
        luc nao cung lo lang vay
        co khi la full cai duoc thi lai chia , con cai khong duoc thi la vang tien , tai sao vay omg c o ma do tai con roi con se co  gwng cai video khong co vai nao kiem tien duoc het die bi bang tieng :(((((
         */

    }

    private void showDialog(int pos, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {

        //custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_ok.setOnClickListener(v -> {

            if (rdi_cancelled != null && rdi_cancelled.isChecked()) {
                updateOrder(pos, orderModel, -1);
                dialog.dismiss();
            } else if (rdi_shipping != null && rdi_shipping.isChecked())//shipping
            {
                //updateOrder(pos,orderModel,1);
                ShipperModel shipperModel = null;
                if (myShipperSelectAdapter != null) {
                    shipperModel = myShipperSelectAdapter.getSelectShipper();
                    if (shipperModel != null) {
                        createShippingOrder(pos, shipperModel, orderModel, dialog);
                    } else
                        Toast.makeText(getContext(), "Please select Shipper", Toast.LENGTH_SHORT).show();

                }
                dialog.dismiss();
            } else if (rdi_shipped != null && rdi_shipped.isChecked()) {
                updateOrder(pos, orderModel, 2);
                dialog.dismiss();
            } else if (rdi_restore_placed != null && rdi_restore_placed.isChecked()) {
                updateOrder(pos, orderModel, 0);
                dialog.dismiss();
            } else if (rdi_delete != null && rdi_delete.isChecked()) {
                deleteOrder(pos, orderModel);
                dialog.dismiss();
            }


        });

    }

    private void createShippingOrder(int pos, ShipperModel shipperModel, OrderModel orderModel, AlertDialog dialog) {
        ShippingOrderModel shippingOrder = new ShippingOrderModel();
        shippingOrder.setShipperPhone(shipperModel.getPhone());
        shippingOrder.setShipperName(shipperModel.getName());
        shippingOrder.setOrderModel(orderModel);
        shippingOrder.setStartTrip(false);
        shippingOrder.setCurrentLat(-1.0);
        shippingOrder.setCurrentLng(-1.0);


        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .child(orderModel.getKey())
                .setValue(shippingOrder)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Log.d("avsd","1");
                    Toast.makeText(getContext(), "Order has been sent to shipper", Toast.LENGTH_SHORT).show();
                    updateOrder(pos, orderModel, 1);
                    sendNotificationToShipper(shipperModel, orderModel);
                }
            }
        });


    }

    private void sendNotificationToShipper(ShipperModel shipperModel, OrderModel orderModel) {
        Log.d("avsd","2");
        // first get token of user
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKENT_REF)
                .child(shipperModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            Log.d("avsd","3");
                            TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                            Map<String, String> notiData = new HashMap<>();
                            notiData.put(Common.NOTI_TITILE, "You have new Order need ship");
                            notiData.put(Common.NOTI_CONTENT, "You have new Order need ship from " +
                                    orderModel.getUserPhone());

                            FCMSendData sendData = new FCMSendData(tokenModel.getToken(), notiData);

                            compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        Toast.makeText(getContext(), "gui thong bao thanh coong", Toast.LENGTH_SHORT).show();
                                        Log.d("avsd","4");
                                    },throwable -> {
                                        Toast.makeText(getContext(), "gui thong bao that bai", Toast.LENGTH_SHORT).show();
                                        Log.d("avsd","5");
                                    })

                            );
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deleteOrder(int pos, OrderModel orderModel) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            adapter.removeItem(pos);
                            adapter.notifyItemRemoved(pos);

                            updateTextCounter();
                            Toast.makeText(getContext(), "delete order Success", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Order number must not be null or null or empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrder(int pos, OrderModel orderModel, int status) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("orderStatus", status);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            adapter.removeItem(pos);
                            adapter.notifyItemRemoved(pos);

                            updateTextCounter();
                            Toast.makeText(getContext(), "Update order Success", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Order number must not be null or null or empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuilder("Order(")
                .append(adapter.getItemCount())
                .append(")"));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_filter) {
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(), "OrderFilter");
            return true;
        } else
            return super.onOptionsItemSelected(item);


        //
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
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
    public void onLoadOrderEvent(LoadOrderEvent event) {
        orderViewModel.loadOrderByStatus(event.getStatus());
    }

    @Override
    public void onShipperLoadSuccess(List<ShipperModel> shipperModelList) {
        //
    }

    @Override
    public void onShipperLoadSuccess12(int pos, OrderModel orderModel, List<ShipperModel> shipperModels, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipper, RadioButton rdi_canceled, RadioButton rdi_delete, RadioButton rdi_restore_place) {
        Log.d("plpl", shipperModels.size() + "");


        recyclerView_shipper.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView_shipper.setLayoutManager(layoutManager);

        myShipperSelectAdapter = new MyShipperSelectionAdapter(getContext(), shipperModels);
        recyclerView_shipper.setAdapter(myShipperSelectAdapter);
        Log.d("plpl", shipperModels.size() + "");

        showDialog(pos, orderModel, dialog, btn_ok, btn_cancel, rdi_shipping, rdi_shipper, rdi_canceled, rdi_delete, rdi_restore_place);
    }


    @Override
    public void onShipperLoadFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }
}















