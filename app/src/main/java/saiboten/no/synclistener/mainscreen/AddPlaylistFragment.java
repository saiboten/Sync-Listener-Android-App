package saiboten.no.synclistener.mainscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import saiboten.no.synclistener.R;

/**
 * Created by Tobias on 03.05.2016.
 */
public class AddPlaylistFragment extends DialogFragment {

    NoticeDialogListener noticeDialogListener;

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(AddPlaylistFragment text);
        public void onDialogNegativeClick(AddPlaylistFragment addPlaylistFragment);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            noticeDialogListener = (NoticeDialogListener) activity;
        } catch(ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.addplaylist, null)).
                setPositiveButton(R.string.alert_okButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noticeDialogListener.onDialogPositiveClick(AddPlaylistFragment.this);
                    }
                })
                .setNegativeButton(R.string.alert_cancelButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        noticeDialogListener.onDialogNegativeClick(AddPlaylistFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}