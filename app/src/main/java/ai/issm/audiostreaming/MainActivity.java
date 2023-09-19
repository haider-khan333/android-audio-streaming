package ai.issm.audiostreaming;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;
    private Button button;

    private WebSocketExample webSocketExample;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        webSocketExample = new WebSocketExample(this);

        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick: button clicked");
//        webSocketExample.sendMessage("Hello from haider");
        webSocketExample.startSendingAudioPackets();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketExample != null) {
            webSocketExample.cleanUp();
        }
    }
}