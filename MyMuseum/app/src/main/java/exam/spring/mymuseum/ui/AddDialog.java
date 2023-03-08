package exam.spring.mymuseum.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import exam.spring.mymuseum.R;

public class AddDialog extends AlertDialog
{
    private Builder dialogBuilder;
    private View view;
    private EditText name,desc,open,close,pos;
    //private ImageButton scan;

    protected AddDialog(Context context, int layoutId)
    {
        super(context);
        LayoutInflater inflater=getLayoutInflater().from(context);
        view=inflater.inflate(layoutId,null);
        dialogBuilder=new Builder(context);
        dialogBuilder.setView(view);
        //dialogBuilder.setCancelable(false);
        name=view.findViewById(R.id.name);
        desc=view.findViewById(R.id.desc);
        open=view.findViewById(R.id.open);
        close=view.findViewById(R.id.close);
        pos=view.findViewById(R.id.pos);
        //shoot=view.findViewById(R.id.shoot);
        //scan=view.findViewById(R.id.scan);
    }

    public void setTitle(int titleId)
    {
        dialogBuilder.setTitle(titleId);
    }

    public void setPositiveButton(int textId, OnClickListener listener)
    {
        dialogBuilder.setPositiveButton(textId, listener);
    }

    public void setNegativeButton(int textId, OnClickListener listener)
    {
        dialogBuilder.setPositiveButton(textId, listener);
    }

    public void setNeutralButton(int textId, OnClickListener listener)
    {
        dialogBuilder.setPositiveButton(textId, listener);
    }

    public void setImageButtonListener(int buttonId,View.OnClickListener listener)
    {
        ImageButton button=view.findViewById(buttonId);
        button.setOnClickListener(listener);
    }

    public void show()
    {
        dialogBuilder.create().show();
    }

    public View getView()
    {
        return view;
    }

    public String getText()
    {
        Toast.makeText(getContext(),desc.getText().toString(),Toast.LENGTH_LONG);
        return desc.getText().toString();
    }


}
