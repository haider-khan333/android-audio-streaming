package ai.issm.audiostreaming;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    // UI Variables
    private TextView responseTextView;
    private TextView recordingStatusTextView;
    private Button button;

    private String message;

    // Custom Variables
    private WebSocketExample webSocketExample;
    private PermissionHandler permissionHandler;

    // Local Variables
    private int AUDIO_PERMISSION_REQ_CODE = 1;
    private final String TAG = "WebSocketClientExample";

    private Disposable disposable;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.recordButton);
        recordingStatusTextView = findViewById(R.id.recordingStatusTextView);
        responseTextView = findViewById(R.id.responseMessageTextView);


        permissionHandler = new PermissionHandler(this);

        webSocketExample = new WebSocketExample(this);
        button.setOnClickListener(this);

        disposable = webSocketExample.getObservableInstance()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message->{
                    responseTextView.setText(message);
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == AUDIO_PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Audio Permission Granted");

            } else {
                Log.i(TAG, "onRequestPermissionsResult: Audio Permission Denied");


            }
        }
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick: button clicked");
        if (permissionHandler.hasRecordAudioPermission()) {
            Log.i(TAG, "onClick: permission already granted");
            webSocketExample.startSendingAudioPackets(true);
        } else {
            Log.i(TAG, "onClick: requesting permission");
            permissionHandler.requestRecordAudioPermission(this, AUDIO_PERMISSION_REQ_CODE);
        }
    }



}