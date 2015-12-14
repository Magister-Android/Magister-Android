package eu.magisterapp.magister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by max on 14-12-15.
 */
public class LoginFixer implements DialogInterface.OnClickListener
{

    private AlertDialog loginDialog;
    private Main main;

    private View dialogView;

    public LoginFixer(Main main)
    {
        this.main = main;
    }

    private AlertDialog getLoginDialog()
    {
        if (dialogView == null)
        {
            dialogView = main.getLayoutInflater().inflate(R.layout.dialog_login, null);

            EditText lastInput = (EditText) dialogView.findViewById(R.id.password_input);

            lastInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((actionId & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_DONE)
                    {
                        onClick(loginDialog, 0);

                        return true;
                    }

                    return false;
                }
            });
        }

        if (loginDialog == null) loginDialog = new AlertDialog.Builder(main)
                .setTitle(R.string.login)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(R.string.login, this)
                .create();

        return loginDialog;
    }

    /**
     * Dit is wat er gebeurd als er op de logindialog op "LOG IN" wordt geklikt.
     *
     * @param dialog  Dialog instance
     * @param which   Event type
     */
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        EditText schoolView = (EditText) dialogView.findViewById(R.id.school_input);
        EditText usernameView = (EditText) dialogView.findViewById(R.id.username_input);
        EditText passwordView = (EditText) dialogView.findViewById(R.id.password_input);

        String school = schoolView.getText().toString();
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        dialog.dismiss();

        login(school, username, password);
    }

    public void login(String school, String username, String password)
    {
        if (! main.getMagisterApplication().hasInternet())
        {
            new AlertDialog.Builder(main)
                    .setMessage(R.string.no_internet)
                    .setCancelable(true)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            startLoginSequence();
                        }
                    })
                    .create().show();
        }

        else
        {
            new LoginTask().execute(school, username, password);
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean>
    {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute()
        {
            progress = new ProgressDialog(main);
            progress.setMessage(main.getString(R.string.login));
            progress.setIndeterminate(true);
            progress.setCancelable(false);

            progress.show();
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            String school = params[0];
            String username = params[1];
            String password = params[2];

            try
            {
                main.getMagisterApplication().getApi().reconnect(school, username, password).login();

                main.getMagisterApplication().updateCredentials(school, username, password);

                return true;
            }

            catch(IOException e)
            {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            progress.dismiss();

            if (success)
            {
                main.postLogin();
            }

            else
            {
                new AlertDialog.Builder(main)
                        .setTitle(R.string.login)
                        .setMessage(R.string.credential_failure)
                        .setCancelable(false)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                startLoginSequence();
                            }
                        })
                        .create().show();
            }
        }
    }

    public void startLoginSequence()
    {
        if (! main.getMagisterApplication().isAuthenticated())
        {
            getLoginDialog().show();
        }

        else
        {
            main.postLogin();
        }
    }
}
