package exam.spring.mymuseum.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import exam.spring.mymuseum.R;

public class CustomAdapter extends ArrayAdapter<CustomItem>
{
    private ArrayList<CustomItem> items;
    private Context context;
    private int resource;

    public CustomAdapter(@NonNull Context context, int resource,ArrayList<CustomItem> items)
    {
        super(context, resource);
        this.items=items;
        this.context=context;
        this.resource=resource;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView=View.inflate(context,resource,null);
        TextView name= convertView.findViewById(R.id.name);
        name.setText(items.get(position).getName());
        return convertView;
    }
}
