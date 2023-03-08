package exam.spring.mymuseum.ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import exam.spring.mymuseum.R;

public class RoomActivity extends AppCompatActivity {

    private static final int CREATE_IMAGE = 0;
    private static final int CAPTURE_IMAGE = 1;
    private static final int CAPTURE_SPEECH = 2;
    private TextView title,desc;//,open,close;
    private ListView objs;
    private ArrayList<CustomItem> objList;
    private Museum museum;
    private Room room;
    private FirebaseApp app;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private Tools tools;
    private Bitmap image;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        tools=new Tools(RoomActivity.this);
        app=FirebaseApp.initializeApp(RoomActivity.this);
        store=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title=findViewById(R.id.name);
        desc=findViewById(R.id.desc);
        //open=findViewById(R.id.open);
        //close=findViewById(R.id.close);
        Bundle bundle=getIntent().getExtras();
        museum=bundle.getParcelable("museum");
        room=bundle.getParcelable("room");
        //Log.d("Room",room.getId());
        title.setText(room.getName());
//        Log.d("ROOM ID",room.getId());
        desc.setText(room.getDescription());
        objs=findViewById(R.id.list);
        objs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(RoomActivity.this,ObjectActivity.class);
                //intent.putExtra("museum",museum);
                //intent.putExtra("room",room);
                intent.putExtra("object",(MuseumObject) objList.get(position));
                startActivity(intent);
            }
        });
        objs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                remove(position);
                return false;
            }
        });
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
    }

    private void add()
    {
        final AddDialog addDialog=new AddDialog(RoomActivity.this,R.layout.object_input);
        addDialog.setTitle(R.string.add_room);
        setButton(addDialog);
        addDialog.show();
    }

    private void setButton(final AddDialog addDialog) {
        addDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    View view=addDialog.getView();
                    final EditText name=view.findViewById(R.id.name);
                    final EditText desc=view.findViewById(R.id.desc);
                    boolean state=!name.getText().toString().equals("") && !desc.getText().toString().equals("") && image!=null;
                    if(!state) throw new IllegalArgumentException(getString(R.string.object_error));
                    HashMap<String,Object> map=new HashMap<String, Object>();
                    map.put("name",name.getText().toString());
                    map.put("description",desc.getText().toString());
                    map.put("photo",tools.convert(image));
                    Log.d("ID",museum.getId());
                    store.collection("museums").document(museum.getId()).collection("rooms").document(room.getId()).collection("objects").add(map)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    tools.toast(R.string.object_added);
                                    MuseumObject object=new MuseumObject(name.getText().toString(),desc.getText().toString(),image);
                                    image=null;
                                    objList.add(object);
                                    readapt();
                                }
                            });
                }
                catch (Exception e)
                {
                    tools.toast(e.getMessage());
                }
            }
        });
        addDialog.setImageButtonListener(R.id.photo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager())!=null)
                    startActivityForResult(intent,CAPTURE_IMAGE);
            }
        });
    }

    private void remove(final int position)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(RoomActivity.this);
        builder.setTitle(R.string.remove_museum);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                store.collection("museums").document(museum.getId())
                        .collection("rooms").document(room.getId())
                        .collection("objects").document(((MuseumObject) objList.get(position)).getId())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        tools.toast(R.string.remove_success);
                        objList.remove(position);
                        readapt();
                    }
                });
            }
        });
        builder.show();
    }

    private void readapt()
    {
        CustomAdapter adapter=new CustomAdapter(RoomActivity.this,R.layout.custom_layout,objList);
        objs.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=auth.getCurrentUser();
        objList=new ArrayList<CustomItem>();
        if(!user.getEmail().equals(museum.getAuthor()))
        {
            fab.setVisibility(View.INVISIBLE);
        }
        try {
            CollectionReference colRef=store.collection("museums")
                    .document(museum.getId()).collection("rooms")
                    .document(room.getId()).collection("objects");
            if(colRef!=null)
            {
                colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc:queryDocumentSnapshots)
                        {
                            String name=doc.getString("name");
                            String desc=doc.getString("description");
                            Bitmap photo=tools.convert(doc.getString("photo"));
                            MuseumObject object=new MuseumObject(name,desc,photo);
                            object.setId(doc.getId() );
                            objList.add(object);
                        }
                        CustomAdapter adapter=new CustomAdapter(RoomActivity.this,R.layout.custom_layout,objList);
                        objs.setAdapter(adapter);
                    }
                });
            }


        }
        catch (Exception e)
        {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String message = "";
            switch (requestCode) {
                case CAPTURE_IMAGE:
                    image = (Bitmap) bundle.get("data");
                    message = getString(R.string.image_added);
                    break;
                case CREATE_IMAGE:
                    image = (Bitmap) bundle.get("data");
                    message = getString(R.string.image_added);
                    break;
                case CAPTURE_SPEECH:
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    desc.setText(result.get(0));
                    break;
            }
            tools.toast(message);
        } else tools.toast(R.string.cancelled);
    }

    private void modify(final int position) throws IOException {
        AddDialog addDialog=new AddDialog(RoomActivity.this,R.layout.museum_input);
        View view=addDialog.getView();
        final EditText name=view.findViewById(R.id.name);
        final EditText desc=view.findViewById(R.id.desc);
        final MuseumObject museumObject=(MuseumObject) objList.get(position);
        name.setText(museumObject.getName());
        desc.setText(museumObject.getDescription());
        addDialog.setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String newName=name.getText().toString();
                    String newDesc=desc.getText().toString();
                    HashMap<String,Object> map=new HashMap<String,Object>();
                    map.put("name",newName);
                    map.put("description",newDesc);
                    map.put("photo",tools.convert(image!=null ? image : museumObject.getPhoto()));
                    image=null;
                    store.collection("museums").document(museum.getId()).collection("rooms").document(room.getId())
                            .collection("objects").document(museumObject.getId()).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            tools.toast(R.string.object_modified);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        addDialog.show();
    }
}
