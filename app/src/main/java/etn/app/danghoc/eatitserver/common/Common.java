package etn.app.danghoc.eatitserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.model.BestDealModel;
import etn.app.danghoc.eatitserver.model.CategoryModel;
import etn.app.danghoc.eatitserver.model.FoodModel;
import etn.app.danghoc.eatitserver.model.OrderModel;
import etn.app.danghoc.eatitserver.model.ServerUserModel;
import etn.app.danghoc.eatitserver.model.TokenModel;

public class Common {

    public static final String CATEGORY_REF = "Category";
    public static final String SERVER_REF = "Server";
    public static final String ORDER_REF = "Order";
    public static final String SHIPPER = "Shippers";
    public static final String SHIPPING_ORDER_REF ="ShippingOrder" ;
    public static final String TOKENT_REF ="Tokens" ;
    public static final String IS_OPEN_ACTIVITY_NEW_ORDER ="IsOpenActivityNewOrder" ;
    public static final String BEST_DEALS ="BestDeals" ;
    public static final String MOST_POPULAR ="MostPopular" ;
    public static final String IS_SEND_IMAGE ="IS_SEND_IMAGE" ;
    public static final String IMAGE_URL ="IMAGE_URL" ;
    public static ServerUserModel currentServerUser;
    public static CategoryModel categorySelected;
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static FoodModel selectedFood;
    private static final String TOKEN_REF = "Tokens";
    public static final String NOTI_TITILE = "Title";
    public static final String NOTI_CONTENT = "Content";
    public static OrderModel currentOrderSelected;
    public static BestDealModel bestDealsSelected;

    public static void setSpanString(String Welcome, String name, TextView txt_user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan bold = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(bold, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        txt_user.setText(builder, TextView.BufferType.SPANNABLE);


    }

    public static void setSpanStringColor(String Welcome, String name, TextView txt_time, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan bold = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(bold, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        txt_time.setText(builder, TextView.BufferType.SPANNABLE);

    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Canceled";
            default:
                return "Error";
        }
    }

    public static void showNotifiCation(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "edmt_dev_eat_v2";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Eat It cc", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Eat It cc");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_restaurant_menu_24));

        if(pendingIntent!=null)
            builder.setContentIntent(pendingIntent);
        Notification notification=builder.build();
        notificationManager.notify(id,notification);
    }


    public static void updateToken(Context  context, String newToken,boolean isServer,boolean isShipper) {
    if (Common.currentServerUser!=null)
    {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new TokenModel(Common.currentServerUser.getPhone(),newToken,isServer,isShipper))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }

    public static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);

        return -1;

    }



    public static String getNewsTopic() {
        return new StringBuilder("/topics/news").toString();
    }
}

/*

 */
