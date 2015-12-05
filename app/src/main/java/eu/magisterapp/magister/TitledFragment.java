package eu.magisterapp.magister;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by max on 13-10-15.
 */
public class TitledFragment extends Fragment
{
    Main main;

    String title;

    Boolean attached = false;

    protected void setTitle(String title)
    {
        // Als dit nog niet geattached is, is er nog geen Main
        // bewaar de title dus eerst en wacht op het onAttach
        // event, en stel daar de title in.
        if (! attached)
        {
            this.title = title;

            return;
        }

        // Als main er wel is, veranderen we de title
        main.changeTitle(title);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        // nu deze bitch aan main is ge-attached, zet main en verander de title.
        main = (Main) context;

        attached = true;

        setTitle(title != null ? title : getAppName());
    }

    protected String getAppName()
    {
        return getResources().getString(R.string.app_name);
    }
}
