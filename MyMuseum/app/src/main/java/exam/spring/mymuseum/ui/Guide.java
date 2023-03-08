package exam.spring.mymuseum.ui;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Guide extends CustomItem implements Parcelable
{
    private String author;
    private ArrayList<Room> rooms;

    public Guide(String name, String description, String author, ArrayList<Room> rooms) {
        this.name = name;
        this.description = description;
        this.author = author;
        this.rooms = rooms;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    protected Guide(Parcel in)
    {
        name=in.readString();
        description=in.readString();
        author=in.readString();
        rooms=(ArrayList<Room>) in.readSerializable();
    }

    public static final Creator<Guide> CREATOR = new Creator<Guide>() {
        @Override
        public Guide createFromParcel(Parcel in) {
            return new Guide(in);
        }

        @Override
        public Guide[] newArray(int size) {
            return new Guide[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(author);
        dest.writeSerializable(rooms);

    }
}
