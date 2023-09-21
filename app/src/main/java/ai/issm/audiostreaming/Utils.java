package ai.issm.audiostreaming;

import org.json.JSONObject;

public class Utils {

    public static JSONObject getAudioJSON(String key, String byteArray) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("packet_no", key);
            jsonObject.put("audio_bytes", byteArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
