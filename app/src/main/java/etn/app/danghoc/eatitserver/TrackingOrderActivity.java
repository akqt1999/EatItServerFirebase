package etn.app.danghoc.eatitserver;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import etn.app.danghoc.eatitserver.Remote.RetrofitGoogleAPIClient;
import etn.app.danghoc.eatitserver.callback.IGoogleAPI;
import etn.app.danghoc.eatitserver.callback.ISingleShippingOrderCallbackListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.ShippingOrderModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback, ISingleShippingOrderCallbackListener, ValueEventListener {

    private GoogleMap mMap;

    //marker
    private Marker shipperMaker;


    private PolylineOptions polylineOptions, blackPolylineOption;
    private List<LatLng> polylineList;
    private Polyline yellowPolyline,grayPolyline,blackPolyline;
    private IGoogleAPI iGoogleAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private ISingleShippingOrderCallbackListener iSingleShippingOrderCallbackListener;
    private ShippingOrderModel currentShippingOrder;
    private DatabaseReference shipperRef;


    //move marker
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;

    private boolean isInit=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
    }

    private void init() {
        iSingleShippingOrderCallbackListener=this;

        iGoogleAPI= RetrofitGoogleAPIClient.getInstance().create(IGoogleAPI.class);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.getUiSettings().setZoomControlsEnabled(true);

        checkOrderFromFirebase();
    }

    private void checkOrderFromFirebase() {
        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .child(Common.currentOrderSelected.getOrderNumber())
                .addListenerForSingleValueEvent(new ValueEventListener() { // nhan cai co san
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            ShippingOrderModel shippingOrderModel=snapshot.getValue(ShippingOrderModel.class);
                            shippingOrderModel.setKey(snapshot.getKey());
                            iSingleShippingOrderCallbackListener.onSingleShippingOrderLoadSuccess(shippingOrderModel);
                        }
                        else
                        {
                            Toast.makeText(TrackingOrderActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrackingOrderActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onSingleShippingOrderLoadSuccess(ShippingOrderModel shippingOrderModel) {


        currentShippingOrder=shippingOrderModel;
        subscribeShipperMove(currentShippingOrder);


        LatLng locationOrder = new LatLng(shippingOrderModel.getOrderModel().getLat(),
                shippingOrderModel.getOrderModel().getLng());
        LatLng locationShipper = new LatLng(shippingOrderModel.getCurrentLat(),
                shippingOrderModel.getCurrentLng());

      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,15));


        // add box
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_box_24))
                .title(shippingOrderModel.getOrderModel().getUserName())
                .snippet(shippingOrderModel.getOrderModel().getShippingAddress())
                .position(locationOrder)
        );
        //add shipper
        if (shipperMaker == null) {
            int height, width;
            height = width = 80;
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                    .getDrawable(TrackingOrderActivity.this, R.drawable.shipper_new);
            Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);

            shipperMaker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resized))
                    .title(shippingOrderModel.getShipperName())
                    .snippet(shippingOrderModel.getShipperPhone())
                    .position(locationShipper)
            );
           // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 10));
        } else {
            shipperMaker.setPosition(locationShipper);
          //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 10));
        }


        //draw router
        String to = new StringBuilder()
                .append(shippingOrderModel.getOrderModel().getLat())
                .append(",")
                .append(shippingOrderModel.getOrderModel().getLng())
                .toString();
        String from = new StringBuilder()
                .append(shippingOrderModel.getCurrentLat())
                .append(",")
                .append(shippingOrderModel.getCurrentLng())
                .toString();


        // api google
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,
                "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {

                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polylineList = Common.decodePoly(polyline);
                                }
                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.RED);
                                polylineOptions.width(12);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polylineList);
                                yellowPolyline = mMap.addPolyline(polylineOptions);

                            } catch (Exception e) {
                                Toast.makeText(TrackingOrderActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            ;
                        }
                        , new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(TrackingOrderActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                )
        );

    }

    private void subscribeShipperMove(ShippingOrderModel currentShippingOrder) {
        shipperRef=FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .child(currentShippingOrder.getKey());

        shipperRef.addValueEventListener(this); // bac su kien khi co thay doi ,
    }



    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

        if(snapshot.exists())
        {

            //save  old position (luu cac gia tri cu)
             String from = new StringBuilder()
                    .append(currentShippingOrder.getCurrentLat())
                    .append(",")
                    .append(currentShippingOrder.getCurrentLng())
                    .toString();

             //update positon
            currentShippingOrder=snapshot.getValue(ShippingOrderModel.class);
            currentShippingOrder.setKey(snapshot.getKey());
            String to = new StringBuilder()
                    .append(currentShippingOrder.getCurrentLat())
                    .append(",")
                    .append(currentShippingOrder.getCurrentLng())
                    .toString();

            if(isInit)
                moveMakerAnimation(shipperMaker,from,to);
            else
                isInit=true;

        }
        else
        {

        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }


    private void moveMakerAnimation(Marker shipperMaker, String from, String to) {
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,"AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8"
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( s->
                {

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Common.decodePoly(polyline);
                        }
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);

                        grayPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOption = new PolylineOptions();
                        blackPolylineOption.color(Color.BLACK);
                        blackPolylineOption.width(5);
                        blackPolylineOption.startCap(new SquareCap());
                        blackPolylineOption.jointType(JointType.ROUND);
                        blackPolylineOption.addAll(polylineList);

                        blackPolyline=mMap.addPolyline(blackPolylineOption);

                        //animator
                        ValueAnimator polylineAnimator=ValueAnimator.ofInt(0,100);
                        polylineAnimator.setDuration(2000);
                        polylineAnimator.setInterpolator(new LinearInterpolator());
                        polylineAnimator.addUpdateListener(animation -> {
                            List<LatLng>points=grayPolyline.getPoints();
                            int percentValue=(int)animation.getAnimatedValue();
                            int size=points.size();
                            int newPoints=(int)(size*(percentValue/100.0f));
                            List<LatLng>p=points.subList(0,newPoints);
                            blackPolyline.setPoints(p);
                        });

                        polylineAnimator.start();

                        //bike  moving
                        handler=new Handler();
                        index=-1;
                        next=1;

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if(index<polylineList.size()-1)
                                {
                                    index++;
                                    next=index+1;
                                    start=polylineList.get(index);
                                    end=polylineList.get(next);
                                }

                                ValueAnimator valueAnimator=ValueAnimator.ofInt(0,1);
                                valueAnimator.setDuration(1500);
                                valueAnimator.setInterpolator(new LinearInterpolator());
                                valueAnimator.addUpdateListener(animation -> {
                                    v=valueAnimator.getAnimatedFraction();
                                    lng=v*end.longitude+(1-v)
                                            *start.longitude;
                                    lat=v*end.latitude+(1-v)
                                            *start.latitude;
                                    LatLng newPos=new LatLng(lat,lng);
                                    shipperMaker.setPosition(newPos);
                                    shipperMaker.setAnchor(0.5f,0.5f);
                                    shipperMaker.setRotation(Common.getBearing(start,newPos));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));

                                });

                                valueAnimator.start();
                                if(index<polylineList.size()-2)//reach destination
                                    handler.postDelayed(this,1500);
                            }
                        },1500);


                    } catch (Exception e) {
                        Toast.makeText(TrackingOrderActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }



                },throwable -> {
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        shipperRef.removeEventListener(this);
        isInit=false;
        super.onDestroy();
    }
}