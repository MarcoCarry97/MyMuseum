package exam.spring.mymuseum.ui;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

class MuseumObject extends CustomItem implements Parcelable
{
    private Bitmap photo;
    private String id;

    public MuseumObject(String name, String description, Bitmap photo)
    {
        this.name = name;
        this.description = description;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    protected MuseumObject(Parcel in) {
        id=in.readString();
        name=in.readString();
        description=in.readString();
        photo=in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<MuseumObject> CREATOR = new Creator<MuseumObject>() {
        @Override
        public MuseumObject createFromParcel(Parcel in) {
            return new MuseumObject(in);
        }

        @Override
        public MuseumObject[] newArray(int size) {
            return new MuseumObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeParcelable(photo,flags);
    }
}
