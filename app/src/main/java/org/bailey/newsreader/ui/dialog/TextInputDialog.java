package org.bailey.newsreader.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TextInputDialog extends DialogFragment {
    public interface OCLwInput {
        void onClick(EditText input, DialogInterface dialog, int which);
    }

    public static final String ARG_TITLE = "Title";
    public static final String ARG_HINT = "Hint";
    public static final String ARG_START_TEXT = "StartText";

    private OCLwInput yes;
    private OCLwInput no;

    public static TextInputDialog newInstance(String title,
                                              String hint,
                                              String startText,
                                              OCLwInput yes,
                                              OCLwInput no) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_HINT, hint);
        args.putString(ARG_START_TEXT, startText);

        TextInputDialog fragment = new TextInputDialog();
        fragment.setArguments(args);
        fragment.yes = yes;
        fragment.no = no;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        assert args != null;

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setHint(args.getString(ARG_HINT));
        input.setText(args.getString(ARG_START_TEXT));

        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getString(ARG_TITLE))
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> yes.onClick(input, dialog, which))
                .setNegativeButton("CANCEL", (dialog, which) -> no.onClick(input, dialog, which))
                .create();
    }
}
