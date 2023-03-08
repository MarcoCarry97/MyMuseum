package exam.spring.mymuseum.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import exam.spring.mymuseum.R;

public class ObjectActivity extends AppCompatActivity {

    private TextView name,desc;
    private ImageView image;
    private MuseumObject museumObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);
        name=findViewById(R.id.name);
        desc=findViewById(R.id.desc);
        image=findViewById(R.id.photo);
        Bundle bundle=getIntent().getExtras();
        museumObject=bundle.getParcelable("object");
        name.setText(museumObject.getName());
        desc.setText(museumObject.getDescription());
        image.setImageBitmap(museumObject.getPhoto());
    }
}
