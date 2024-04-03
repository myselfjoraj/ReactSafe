package jr.project.reactsafe.extras.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jr.project.reactsafe.BlockedInfoActivity;

@SuppressLint("SimpleDateFormat")
public class Extras {


    public static long getTimestamp(){
        return Calendar.getInstance().getTimeInMillis();
    }

    //get current date in dd mmmm yyyy
    public static String date(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        return formatter.format(date);
    }

    public static String time(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getStandardFormDateFromTimeStamp(String timeStamp){
        DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        long milliSeconds= Long.parseLong(timeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getTimeFromTimeStamp(String timeStamp){
        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        long milliSeconds= Long.parseLong(timeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static BitmapDescriptor bitmapFromVector(Context context, int vectorResId)
    {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(
                context, vectorResId);

        // below line is use to set bounds to our vector
        // drawable.
        vectorDrawable.setBounds(
                0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our
        // bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static String getLocationString(Context context,String lat,String lng){
        String loc = lat + ", " +lng;
        try {

            double lati = Double.parseDouble(lat);
            double longi = Double.parseDouble(lng);

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            addresses = geocoder.getFromLocation(lati, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            if (city != null && !city.equals("null") && state != null &&!state.equals("null") ){
                loc = lat + ", " +lng;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return loc;

    }

    public static void transferToBlocked(Context context){
        context.startActivity(new Intent(context, BlockedInfoActivity.class));
    }

}
