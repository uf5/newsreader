package org.bailey.newsreader.ui.adapter;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispChannel;
import org.bailey.newsreader.channels.UserChannels;
import org.bailey.newsreader.ui.activity.MainActivity;
import org.bailey.newsreader.ui.activity.ScriptingActivity;
import org.bailey.newsreader.ui.dialog.TextInputDialog;
import org.bailey.newsreader.ui.fragment.ChannelsFragment;
import org.bailey.newsreader.ui.fragment.PostFragment;
import org.bailey.newsreader.ui.fragment.PostsFragment;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

// ChannelRecyclerAdapter - класс RecyclerAdapter при помощи которого осуществляется отображение
// списка пользовательских каналов.
public class ChannelsRecyclerAdapter extends RecyclerView.Adapter<ChannelsRecyclerAdapter.ChannelViewHolder> {

    @NotNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_channel, viewGroup, false);

        return new ChannelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder viewHolder, int position) {
        viewHolder.textView1.setText(UserChannels.get(position).name);
        viewHolder.itemView.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            UserChannels.selectedChannel = UserChannels.get(viewHolder.getAbsoluteAdapterPosition());
            // Used only to make the code look prettier
            LispChannel selChannel = UserChannels.selectedChannel;

            if (selChannel.evalThread == null ||
                    (selChannel.staleAST &&
                            selChannel.evalThread.getState() == Thread.State.TERMINATED) ||
                    selChannel.evalThread.getState() == Thread.State.RUNNABLE) {

                selChannel.prepareThread();
                navController.navigate(R.id.action_channelsFragment_to_loadingFragment);
            } else {
                if (selChannel.isPosts) {
                    Bundle channelPostsBundle = new Bundle();
                    channelPostsBundle.putString(PostsFragment.TAG_CHANNEL_NAME,
                            selChannel.name);
                    navController.navigate(R.id.action_channelsFragment_to_postsFragment, channelPostsBundle);
                } else {
                    Bundle postBundle = new Bundle();
                    postBundle.putString(PostFragment.TAG_TITLE,
                            MainActivity.appContext.getString(R.string.evaluation_result));
                    postBundle.putString(PostFragment.TAG_TEXT,
                            selChannel.lastResult.toString());
                    navController.navigate(R.id.action_channelsFragment_to_postFragment, postBundle);
                }
            }
        });
        viewHolder.itemView.setOnLongClickListener(view -> {
            viewHolder.channelActionsDialog();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return UserChannels.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView1;

        public ChannelViewHolder(@NonNull View v) {
            super(v);
            textView1 = v.findViewById(R.id.channel_item_text);
        }

        public void channelActionsDialog() {
            Resources res = MainActivity.appContext.getResources();
            String[] items = Arrays.stream(new int[]{
                    R.string.action_refresh,
                    R.string.menu_channel_title_rename,
                    R.string.menu_channel_title_duplicate,
                    R.string.menu_channel_title_edit,
                    R.string.menu_channel_title_delete,
            }).mapToObj(res::getString).toArray(String[]::new);

            int id = getBindingAdapterPosition();
            LispChannel currentChannel = UserChannels.get(id);
            new MaterialAlertDialogBuilder(MainActivity.instance)
                    .setTitle(currentChannel.name)
                    .setItems(items, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                currentChannel.startEval();
                                break;
                            case 1:
                                TextInputDialog.newInstance(
                                        res.getString(R.string.dialog_rename_title),
                                        res.getString(R.string.dialog_name),
                                        currentChannel.name,
                                        (input, dialog1, which1) -> {
                                            currentChannel.name = input.getText().toString();
                                            UserChannels.update(id);
                                            ChannelsFragment.adapter.notifyItemChanged(id);
                                        },
                                        (input, dialog1, which1) -> dialog1.cancel())
                                        .show(MainActivity.instance.getSupportFragmentManager(),
                                                "TextInputDialog");
                                UserChannels.update(id);
                                ChannelsFragment.adapter.notifyItemChanged(id);
                                break;
                            case 2:
                                UserChannels.insert(new LispChannel(currentChannel.name,
                                                currentChannel.script),
                                        id + 1);
                                ChannelsFragment.adapter.notifyItemInserted(id + 1);
                                break;
                            case 3:
                                Intent intent = new Intent(MainActivity.appContext, ScriptingActivity.class);
                                intent.putExtra(ScriptingActivity.ARG_INDEX, id);
                                MainActivity.instance.startActivity(intent);
                                break;
                            case 4:
                                UserChannels.remove(id);
                                ChannelsFragment.adapter.notifyItemRemoved(id);
                                break;
                        }
                    })
                    .show();
        }
    }
}