package org.bailey.newsreader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.bailey.newsreader.R;
import org.jetbrains.annotations.NotNull;

// PostFragment - класс Fragment используемый для отображения выбранного поста.
public class PostFragment extends Fragment {

    public static final String TAG_TITLE = "title";
    public static final String TAG_TEXT = "text";

    private String title;
    private String text;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        assert args != null;
        title = args.getString(TAG_TITLE);
        text = args.getString(TAG_TEXT);

        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        ((TextView) rootView.findViewById(R.id.post_title)).setText(title);
        ((TextView) rootView.findViewById(R.id.post_text)).setText(text);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_post, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.share_post) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + text);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}