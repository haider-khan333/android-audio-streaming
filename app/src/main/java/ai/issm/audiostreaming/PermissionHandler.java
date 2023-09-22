package ai.issm.audiostreaming;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;

public class PermissionHandler {

    private final Context context;


    public PermissionHandler(Context context) {
        this.context = context;
    }

    public boolean hasRecordAudioPermission() {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public void requestRecordAudioPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}
                , requestCode);
    }

}
