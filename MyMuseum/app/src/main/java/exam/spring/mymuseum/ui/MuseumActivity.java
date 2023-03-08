package exam.spring.mymuseum.ui;

import android.content.DialogInterface;
import android.content.Intent;
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

public class MuseumActivity extends AppCompatActivity {

    private TextView title,desc;//,open,close;
    private ListView rooms;
    private ArrayList<CustomItem> roomList;
    private Museum museum;
    private FirebaseApp app;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private Tools tools;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum);
        tools=new Tools(MuseumActivity.this);
        app=FirebaseApp.initializeApp(MuseumActivity.this);
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
        title.setText(museum.getName());
        Log.d("MUSEUM ID",museum.getId());
        desc.setText(museum.getDescription());
        rooms=findViewById(R.id.list);
        rooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MuseumActivity.this,RoomActivity.class);
                intent.putExtra("museum",museum);
                intent.putExtra("room",(Room) roomList.get(position));
                startActivity(intent);
            }
        });
        rooms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
        final AddDialog addDialog=new AddDialog(MuseumActivity.this,R.layout.custom_input);
        addDialog.setTitle(R.string.add_room);
        addDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    View view=addDialog.getView();
                    final EditText name=view.findViewById(R.id.name);
                    final EditText desc=view.findViewById(R.id.desc);
                    if(name.getText().toString().equals("") || name.getText().toString().equals(""))
                        throw new IllegalArgumentException(getString(R.string.add_error));
                    HashMap<String,Object> map=new HashMap<String, Object>();
                    map.put("name",name.getText().toString());
                    map.put("description",desc.getText().toString());
                    Log.d("ID",museum.getId());
                    store.collection("museums").document(museum.getId()).collection("rooms").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            tools.toast(R.string.room_added);
                            Room room=new Room(name.getText().toString(),desc.getText().toString());
                            roomList.add(room);
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
        addDialog.show();
    }

    private void remove(final int position)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(MuseumActivity.this);
        builder.setTitle(R.string.remove_museum);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                store.collection("museums").document( ((Room) roomList.get(position)).getId())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        tools.toast(R.string.remove_success);
                        roomList.remove(position);
                        readapt();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.modify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    modify(position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
    }

    private void readapt()
    {
        CustomAdapter adapter=new CustomAdapter(MuseumActivity.this,R.layout.custom_layout,roomList);
        rooms.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=auth.getCurrentUser();
        roomList=new ArrayList<CustomItem>();
        Log.d(user.getEmail(),museum.getAuthor());
        if(!user.getEmail().equals(museum.getAuthor()))
        {
            fab.setVisibility(View.INVISIBLE);
        }
        try {
            CollectionReference colRef=store.collection("museums").document(museum.getId()).collection("rooms");
            if(colRef!=null)
            {
                colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc:queryDocumentSnapshots)
                        {
                            String name=doc.getString("name");
                            String desc=doc.getString("description");
                            Room room=new Room(name,desc);
                            room.setId(doc.getId());
                            roomList.add(room);
                        }
                        CustomAdapter adapter=new CustomAdapter(MuseumActivity.this,R.layout.custom_layout,roomList);
                        rooms.setAdapter(adapter);
                    }
                });
            }


        }
        catch (Exception e)
        {
            tools.toast(e.getMessage());
        }
    }

    private void modify(final int position) throws IOException {
        AddDialog addDialog=new AddDialog(MuseumActivity.this,R.layout.custom_input);
        View view=addDialog.getView();
        final EditText name=view.findViewById(R.id.name);
        final EditText desc=view.findViewById(R.id.desc);
        final Room room=(Room) roomList.get(position);
        name.setText(room.getName());
        desc.setText(room.getDescription());
        addDialog.setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String newName=name.getText().toString();
                    String newDesc=desc.getText().toString();
                    HashMap<String,Object> map=new HashMap<String,Object>();
                    map.put("name",newName);
                    map.put("description",newDesc);
                    //map.put("latitude",newPos.latitude);
                    //map.put("longitude",newPos.longitude);
                    //map.put("author",user.getEmail());
                    store.collection("museums").document(museum.getId())
                            .collection("rooms")
                            .document(room.getId()).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            tools.toast(R.string.room_modified);
                        }
                    });
                } catch (Exception e) {
                    //tools.toast(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        addDialog.show();
    }
}
