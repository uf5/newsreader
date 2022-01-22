package org.bailey.newsreader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.bailey.newsreader.R;
import org.bailey.newsreader.channels.LispChannel;
import org.bailey.newsreader.channels.UserChannels;

// ScriptingActivity - класс Activity используемый для редактирования скриптов.
public class ScriptingActivity extends AppCompatActivity {

    public static final String ARG_INDEX = "LispScriptIndex";

    private LispChannel channel;
    private int channelID;
    private EditText scriptEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        channelID = intent.getIntExtra(ARG_INDEX, -1);
        channel = UserChannels.get(channelID);
        setContentView(R.layout.activity_scripting);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(String.format(getResources().getString(R.string.title_script), channel.name));
        actionBar.setDisplayHomeAsUpEnabled(true);

        scriptEditText = findViewById(R.id.script_et);

        scriptEditText.setText(channel.script);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scripting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_script_save) {
            channel.setScript(scriptEditText.getText().toString());
            UserChannels.update(channelID);
            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                    String.format(getString(R.string.saved_msg), channel.name),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else if (itemId == R.id.action_script_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, scriptEditText.getText().toString());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}