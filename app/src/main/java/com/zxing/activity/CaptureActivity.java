package com.zxing.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.letmefold.R;
import com.letmefold.utils.UriUtil;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.decoding.RGBLuminanceSource;
import com.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import static com.letmefold.Config.INTENT_EXTRA_KEY_QR_SCAN;
import static com.letmefold.utils.Util.immersion;

/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class CaptureActivity extends AppCompatActivity implements Callback, View.OnClickListener {

    private static final int REQUEST_CODE_SCAN_GALLERY = 100;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private ImageButton back;
    private ImageButton btnFlash;
    private TextView btnAlbum;
    private boolean isFlashOn = false;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ProgressDialog mProgress;
    private String photoPath;
    private TextView title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        immersion(this, getResources().getColor(R.color.qmui_config_color_blue), false);
        CameraManager.init(getApplication());
        findView();
        addListener();
        title.setText("扫描二维码");
        btnAlbum.setText("相册");
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void findView() {
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_content);
        back = (ImageButton) findViewById(R.id.btn_back);
        btnFlash = (ImageButton) findViewById(R.id.btn_flash);
        btnAlbum = (TextView) findViewById(R.id.btn_album);
        title = (TextView) findViewById(R.id.txt_title);
    }

    private void addListener() {
        back.setOnClickListener(this);
        btnFlash.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (back == v) {
            finish();
        } else if (btnFlash == v) {
            //闪光灯开关按钮
            try {
                boolean isSuccess = CameraManager.get().setFlashLight(!isFlashOn);
                if (!isSuccess) {
                    Toast.makeText(CaptureActivity.this, "暂时无法开启闪光灯", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isFlashOn) {
                    // 关闭闪光灯
                    btnFlash.setImageResource(R.drawable.flash_off);
                    isFlashOn = false;
                } else {
                    // 开启闪光灯
                    btnFlash.setImageResource(R.drawable.flash_on);
                    isFlashOn = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (btnAlbum == v) {
            //打开手机中的相册
            //"android.intent.action.GET_CONTENT"
            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            innerIntent.setType("image/*");
            startActivityForResult(innerIntent, REQUEST_CODE_SCAN_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SCAN_GALLERY) {
                handleAlbumPic(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理选择的图片
     */
    private void handleAlbumPic(Intent data) {
        //获取选中图片的路径
        photoPath = UriUtil.getRealPathFromUri(CaptureActivity.this, data.getData());

        mProgress = new ProgressDialog(CaptureActivity.this);
        mProgress.setMessage("正在扫描...");
        mProgress.setCancelable(false);
        mProgress.show();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.dismiss();
                Result result = scanningImage(photoPath);
                if (result != null) {
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString(INTENT_EXTRA_KEY_QR_SCAN, result.getText());

                    resultIntent.putExtras(bundle);
                    CaptureActivity.this.setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(CaptureActivity.this, "识别失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 扫描二维码图片的方法
     */
    private Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        //设置二维码内容的编码
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

        BitmapFactory.Options options = new BitmapFactory.Options();
        // 先获取原大小
        options.inJustDecodeBounds = true;
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        // 获取新的大小
        options.inJustDecodeBounds = false;
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0) {
            sampleSize = 1;
        }
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.scanner_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService != null && audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(INTENT_EXTRA_KEY_QR_SCAN, resultString);
            // 不能使用Intent传递大于40kb的bitmap，可以使用一个单例对象存储这个bitmap
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
        }
        CaptureActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException | RuntimeException ioe) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
}