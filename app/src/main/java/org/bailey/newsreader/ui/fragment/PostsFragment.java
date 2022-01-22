package org.bailey.newsreader.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.UserChannels;
import org.bailey.newsreader.ui.adapter.PostsRecyclerAdapter;

// PostsFragment - класс Fragment используемый для взаимодействия со списком постов в канале.
public class PostsFragment extends Fragment {

    public static final String TAG_CHANNEL_NAME = "channel_name";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        RecyclerView postsRecycler = rootView.findViewById(R.id.recycler_posts);

        PostsRecyclerAdapter postsAdapter = new PostsRecyclerAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        postsRecycler.setAdapter(postsAdapter);
        postsRecycler.setLayoutManager(layoutManager);


        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.posts_swipe_refresh);
        refreshLayout.setOnRefreshListener(
                () -> {
                    UserChannels.selectedChannel.prepareThread();
                    UserChannels.selectedChannel.onEvalEnd = () -> {
                        postsAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    };
                    UserChannels.selectedChannel.evalThread.start();
                }
        );
        return rootView;
    }
}