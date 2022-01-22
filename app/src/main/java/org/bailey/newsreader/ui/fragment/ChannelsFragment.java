package org.bailey.newsreader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispChannel;
import org.bailey.newsreader.channels.UserChannels;
import org.bailey.newsreader.ui.activity.LogActivity;
import org.bailey.newsreader.ui.activity.MainActivity;
import org.bailey.newsreader.ui.adapter.ChannelsRecyclerAdapter;
import org.bailey.newsreader.ui.dialog.TextInputDialog;
import org.jetbrains.annotations.NotNull;

// ChannelsFragment - класс Fragment используемый для взаимодействия с пользовательскими каналами.
public class ChannelsFragment extends Fragment {

    public RecyclerView channelsRecycler;
    public static ChannelsRecyclerAdapter adapter;
    @Nullable
    public RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_channels, container, false);
        channelsRecycler = rootView.findViewById(R.id.recycler_channels);

        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ChannelsRecyclerAdapter();

        channelsRecycler.setAdapter(adapter);
        channelsRecycler.setLayoutManager(layoutManager);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = view.findViewById(R.id.board_fab);

        fab.setOnClickListener(view1 -> TextInputDialog.newInstance(
                getString(R.string.dialog_new_script_title),
                getString(R.string.dialog_name),
                "",
                (input, dialog, which) -> {
                    UserChannels.add(new LispChannel(input.getText().toString(), ""));
                    adapter.notifyItemInserted(UserChannels.size() - 1);
                },
                (input, dialog, which) -> dialog.cancel())
                .show(MainActivity.instance.getSupportFragmentManager(), "TextInputDialog"));
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh)
            for (int i = 0; i < UserChannels.size(); i++) UserChannels.get(i).startEval();
        else if (itemId == R.id.action_log)
            startActivity(new Intent(getContext(), LogActivity.class));
        return super.onOptionsItemSelected(item);
    }
}