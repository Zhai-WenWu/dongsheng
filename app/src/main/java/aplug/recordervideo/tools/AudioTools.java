package aplug.recordervideo.tools;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by XiangHa on 2016/10/12.
 */
public class AudioTools {
    public static void play(Context con, final OnPlayAudioListener listener,int resid){
        MediaPlayer mediaPlayer = MediaPlayer.create(con, resid);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (listener != null)listener.playOver();
            }
        });
        mediaPlayer.start();
    }

    public interface OnPlayAudioListener{
        public void playOver();
    }
}
