package org.bailey.newsreader.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispPost;
import org.bailey.newsreader.channels.UserChannels;
import org.bailey.newsreader.ui.fragment.PostFragment;
import org.jetbrains.annotations.NotNull;

// PostsRecyclerAdapter - класс RecyclerAdapter при помощи которого осуществляется отображение
// списка постов в выбранном канале.
public class PostsRecyclerAdapter extends RecyclerView.Adapter<PostsRecyclerAdapter.PostsViewHolder> {

    @NotNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_posts, viewGroup, false);

        return new PostsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {
        LispPost post = UserChannels.selectedChannel.posts[position];
        holder.titleTextView.setText(post.title);
        holder.textTextView.setText(post.text);
        holder.itemView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.TAG_TITLE, post.title);
            bundle.putString(PostFragment.TAG_TEXT, post.text);
            Navigation.findNavController(view).navigate(R.id.action_postsFragment_to_postFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return UserChannels.selectedChannel.posts.length;
    }

    static class PostsViewHolder extends RecyclerView.ViewHolder {

        public final TextView titleTextView;
        public final TextView textTextView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.posts_item_title);
            textTextView = itemView.findViewById(R.id.posts_item_text);
        }
    }
}
