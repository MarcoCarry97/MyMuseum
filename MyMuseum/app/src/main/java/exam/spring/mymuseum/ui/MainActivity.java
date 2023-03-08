package exam.spring.mymuseum.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import exam.spring.mymuseum.R;

public class MainActivity extends AppCompatActivity {

    private ListView museums;
    private FirebaseApp app;
    private FirebaseFirestore store;
    private ArrayList<Museum> museumList;
    private Tools tools;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        shared=getPreferences(Context.MODE_PRIVATE);
        tools=new Tools(MainActivity.this);
        setSupportActionBar(toolbar);
        museums=findViewById(R.id.list);
        museums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this,MuseumActivity.class);
                intent.putExtra("museum",museumList.get(position));
                Log.d("MAIN ID",museumList.get(position).getId());
                startActivity(intent);
            }
        });
        museums.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                remove(position);
                return false;
            }
        });
        app=FirebaseApp.initializeApp(MainActivity.this);
        store=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AddDialog addDialog=new AddDialog(MainActivity.this,R.layout.museum_input);
                addDialog.setTitle(R.string.add_museum);
                addDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        add(addDialog);
                    }
                });
                addDialog.show();
            }
        });
    }

    private void add(AddDialog dialog)
    {
       try {
           View view=dialog.getView();
           EditText editName=view.findViewById(R.id.name);
           EditText editDesc=view.findViewById(R.id.desc);
           //EditText editOpen=view.findViewById(R.id.open);
           //EditText editClose=view.findViewById(R.id.close);
           EditText editPos=view.findViewById(R.id.pos);
           String name=editName.getText().toString();
           String desc=editDesc.getText().toString();
           //SimpleDateFormat format=new SimpleDateFormat("hh:mm");
          // Time open= new Time(format.parse(editOpen.getText().toString()).getTime());
           //Time close= new Time(format.parse(editClose.getText().toString()).getTime());
           LatLng position=tools.geocode(editPos.getText().toString());
           if(name.equals("") || desc.equals("") || position==null)
               throw new IllegalArgumentException(getString(R.string.add_error));
           HashMap<String,Object> map=new HashMap<String, Object>();
           final Museum museum=new Museum(name,desc,position,user.getEmail());
           map.put("name",name);
           map.put("description",desc);
           map.put("latitude",position.latitude);
           map.put("longitude",position.longitude);
           map.put("author",user.getEmail());
           //map.put("open",open);
           //map.put("close",close);
           store.collection("museums").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
               @Override
               public void onSuccess(DocumentReference documentReference) {
                   tools.toast(R.string.museum_added);
                   museum.setId(documentReference.getId());
                    museumList.add(museum);
                    readapt();

               }
           });
       }
       catch (IOException e) {
           e.printStackTrace();
       }
       catch (IllegalArgumentException e)
       {
           tools.toast(e.getMessage());
       }
    }

    private void readapt()
    {
        MuseumAdapter adapter=new MuseumAdapter(MainActivity.this,R.layout.museum_tile,museumList);
        museums.setAdapter(adapter);
    }

    private void remove(final int position)
    {
        Museum museum=museumList.get(position);
        if(user.getEmail().equals(museum.getAuthor()))
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.decision);
            builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    store.collection("museums").document(museumList.get(position).getId())
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            tools.toast(R.string.remove_success);
                            museumList.remove(position);
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
        else tools.toast("You can't modify this museum!");

    }

    private void modify(final int position) throws IOException {
        AddDialog addDialog=new AddDialog(MainActivity.this,R.layout.museum_input);
        View view=addDialog.getView();
        final EditText name=view.findViewById(R.id.name);
        final EditText desc=view.findViewById(R.id.desc);
        final EditText pos=view.findViewById(R.id.pos);
        final Museum mus=museumList.get(position);
        name.setText(mus.getName());
        desc.setText(mus.getDescription());
        pos.setText(tools.geocode(mus.getPosition()));
        addDialog.setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final String newName=name.getText().toString();
                    final String newDesc=desc.getText().toString();
                    final LatLng newPos=tools.geocode(pos.getText().toString());
                    final HashMap<String,Object> map=new HashMap<String,Object>();
                    map.put("name",newName);
                    map.put("description",newDesc);
                    map.put("latitude",newPos.latitude);
                    map.put("longitude",newPos.longitude);
                    map.put("author",user.getEmail());
                    store.collection("museums").document(museumList.get(position).getId()).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            tools.toast(R.string.museum_modified);
                            Museum museum=museumList.get(position);
                            museum.setName(newName);
                            museum.setDescription(newDesc);
                            museum.setPosition(newPos);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        addDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=auth.getCurrentUser();
        museumList=new ArrayList<Museum>();
        store.collection("museums").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot doc:queryDocumentSnapshots.getDocuments())
                {
                    String name=doc.getString("name");
                    String desc=doc.getString("description");
                   // Time open=new Time(doc.getTimestamp("open").getSeconds());
                   // Time close=new Time(doc.getTimestamp("close").getSeconds());
                    double lat=doc.getDouble("latitude");
                    double lng=doc.getDouble("longitude");
                    LatLng position=new LatLng(lat,lng);
                    String author=doc.getString("author");
                    Museum museum=new Museum(name,desc,position,author);
                    Log.d("DOC ID", doc.getId());
                    museum.setId(doc.getId());
                    museumList.add(museum);
                }
                MuseumAdapter adapter=new MuseumAdapter(MainActivity.this,R.layout.museum_tile,museumList);
                museums.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.map)
        {
            Intent intent=new Intent(MainActivity.this,MapsActivity.class);
            intent.putParcelableArrayListExtra("museums",museumList);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.logout)
        {
            Intent result=new Intent();
            setResult(RESULT_OK,result);
            finish();
        }
        return true;
    }
}
