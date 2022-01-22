package org.bailey.newsreader.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispChannel;
import org.bailey.newsreader.channels.UserChannels;

// LoadingFragment - класс Fragment используемый для отображения того, что выбранный пользователем
// скрипт выполняется. Пользователь может прервать выполнение скрипта.
// По окончанию выполнения скрипта фрагмент перенаправяется пользователя на следующий фрагмент
// (PostsFragment или PostFragment).
public class LoadingFragment extends Fragment {

    public LoadingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loading, container, false);

        rootView.findViewById(R.id.button_stop_evaluation).setOnClickListener(
                view -> UserChannels.selectedChannel.evalThread.interrupt()
        );

        return rootView;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        UserChannels.selectedChannel.onEvalEnd = () -> {
            View view = getView();
            assert view != null;
            NavController navController = Navigation.findNavController(view);
            LispChannel selChannel = UserChannels.selectedChannel;
            if (selChannel.isPosts) {
                Bundle channelPostsBundle = new Bundle();
                channelPostsBundle.putString(PostsFragment.TAG_CHANNEL_NAME,
                        selChannel.name);
                navController.navigate(R.id.action_loadingFragment_to_postsFragment, channelPostsBundle);
            } else {
                Bundle postBundle = new Bundle();
                postBundle.putString(PostFragment.TAG_TITLE, getString(R.string.evaluation_result));
                postBundle.putString(PostFragment.TAG_TEXT, selChannel.lastResult.toString());
                navController.navigate(R.id.action_loadingFragment_to_postFragment, postBundle);
            }
        };
        UserChannels.selectedChannel.evalThread.start();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        UserChannels.selectedChannel.onEvalEnd = null;
        super.onPause();
    }
}