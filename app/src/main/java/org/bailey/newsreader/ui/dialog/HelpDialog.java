package org.bailey.newsreader.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import org.bailey.newsreader.R;
import org.jetbrains.annotations.NotNull;

public class HelpDialog extends DialogFragment {
    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.help_title))
                .setMessage(getString(R.string.help_text))
                .setPositiveButton("OK", (dialog, id) -> {
                });
        return builder.create();
    }
}
