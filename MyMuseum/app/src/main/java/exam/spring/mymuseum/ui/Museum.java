package exam.spring.mymuseum.ui;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.ArrayList;

public class Museum implements Parcelable
{
    private String id;
    private String name;
    private String description;
    private Time open;
    private Time close;
    private LatLng position;
    private String author;

    public Museum(String name, String description, LatLng position,String author)
    {
        this.name = name;
        this.description = description;
        this.open = open;
        this.close = close;
        this.position = position;
        this.author=author;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Time getOpen() {
        return open;
    }

    public void setOpen(Time open) {
        this.open = open;
    }

    public Time getClose() {
        return close;
    }

    public void setClose(Time close) {
        this.close = close;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    protected Museum(Parcel in)
    {
        author=in.readString();
        id=in.readString();
        name=in.readString();
        description=in.readString();
      //  open=new Time(in.readLong());
       // close=new Time(in.readLong());
        position=in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Museum> CREATOR = new Creator<Museum>() {
        @Override
        public Museum createFromParcel(Parcel in) {
            return new Museum(in);
        }

        @Override
        public Museum[] newArray(int size) {
            return new Museum[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(author);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
//        dest.writeLong(open.getTime());
  //      dest.writeLong(close.getTime());
        dest.writeParcelable(position,flags);
    }
}
