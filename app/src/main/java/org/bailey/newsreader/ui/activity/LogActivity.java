package org.bailey.newsreader.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispLogger;
import org.bailey.newsreader.ui.adapter.LogRecyclerAdapter;

// LogActivity - класс Activity отображающий список лог-сообщений.
public class LogActivity extends AppCompatActivity {

    private LogRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        RecyclerView logRecycler = findViewById(R.id.recycler_log);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new LogRecyclerAdapter();

        logRecycler.setAdapter(adapter);
        logRecycler.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.log_action_clear) {
            LispLogger.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
}