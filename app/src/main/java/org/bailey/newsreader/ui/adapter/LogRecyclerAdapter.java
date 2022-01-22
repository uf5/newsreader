package org.bailey.newsreader.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispLogger;
import org.bailey.newsreader.ui.activity.MainActivity;
import org.jetbrains.annotations.NotNull;

import kotlin.Pair;

// LogRecyclerAdapter - класс RecyclerAdapter при помощи которого осуществляется отображение
// списка лог-сообщений.
public class LogRecyclerAdapter extends RecyclerView.Adapter<LogRecyclerAdapter.LogViewHolder> {

    @NonNull
    @NotNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);

        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull LogViewHolder holder, int position) {
        Pair<LispLogger.LogTag, String> current = LispLogger.contents.get(position);
        holder.tagTextView.setText(current.getFirst().toString());
        final int color;
        switch (current.getFirst()) {
            case PRINT:
                color = MainActivity.appContext.getColor(R.color.log_print);
                break;

            case WARN:
                color = MainActivity.appContext.getColor(R.color.log_warn);
                break;

            case ERROR:
                color = MainActivity.appContext.getColor(R.color.log_err);
                break;

            default:
                color = MainActivity.appContext.getColor(R.color.log_debug);
                break;
        }
        holder.tagTextView.setBackgroundColor(color);
        holder.msgTextView.setText(current.getSecond());
    }

    @Override
    public int getItemCount() {
        return LispLogger.contents.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        public final TextView tagTextView;
        public final TextView msgTextView;

        public LogViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tagTextView = itemView.findViewById(R.id.log_item_tag);
            msgTextView = itemView.findViewById(R.id.log_item_msg);
        }
    }
}
