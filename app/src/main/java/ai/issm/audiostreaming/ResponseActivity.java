package ai.issm.audiostreaming;

import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResponseActivity extends AppCompatActivity {

    private TextView responseTextView;


    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.response_activity);

        responseTextView = findViewById(R.id.textView);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        responseTextView.setText(message);


    }
}
