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

public class MuseumAdapter extends ArrayAdapter<Museum>
{
    private ArrayList<Museum> museums;
    private Context context;
    private int resource;

    public MuseumAdapter(@NonNull Context context, int resource,ArrayList<Museum> museums) {
        super(context, resource);
        this.museums=museums;
        this.context=context;
        this.resource=resource;
    }

    @Override
    public int getCount() {
        return museums.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView=View.inflate(context,resource,null);
        TextView title=convertView.findViewById(R.id.name);
        TextView open=convertView.findViewById(R.id.open);
        TextView close=convertView.findViewById(R.id.close);
        title.setText(museums.get(position).getName());
//        title.setText(museums.get(position).getOpen().toString());
  //      title.setText(museums.get(position).getClose().toString());
        return convertView;
    }
}
