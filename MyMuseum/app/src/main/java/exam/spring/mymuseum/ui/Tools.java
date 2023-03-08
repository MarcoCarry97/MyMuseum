package exam.spring.mymuseum.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.util.Base64;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tools
{
    public static final int LOGOUT = 1;
    private Context context;

    public Tools(Context context)
    {
        this.context=context;
    }

    public void toast(int messageId)
    {
        toast(context.getString(messageId));
    }

    public void toast(String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        if(base64Str!=null)
        {
            byte[] bytes=Base64.decode(base64Str,Base64.DEFAULT);
            Bitmap image=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            return image;
        }
        else return null;
    }

    public static String convert(Bitmap bitmap)
    {
       if(bitmap!=null)
       {
           ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
           byte[] byteArray = byteArrayOutputStream .toByteArray();
           String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
           return encoded;
       }
       else return null;
    }

    public String geocode(LatLng position) throws IOException {
        Geocoder coder=new Geocoder(context);
        List<Address> addresses=coder.getFromLocation(position.latitude,position.longitude,1);
        if(addresses.size()==0) throw new IOException();
        Address address=addresses.get(0);
        return address.getLocality();
    }

    public LatLng geocode(String street) throws IOException {
        Geocoder coder=new Geocoder(context);
        List<Address> addresses=coder.getFromLocationName(street,1);
        if(addresses.size()==0) throw new IOException();
        Address address=addresses.get(0);
        LatLng position=new LatLng(address.getLatitude(),address.getLongitude());
        return position;
    }
}
