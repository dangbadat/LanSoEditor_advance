package com.example.advanceDemo.scene;


import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageTransformFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBlackFilter;

import com.example.advanceDemo.VideoPlayerActivity;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.FileParameter;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoplayer.VPlayer;
import com.lansosdk.videoplayer.VideoPlayer;
import com.lansosdk.videoplayer.VideoPlayer.OnPlayerPreparedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Video2LayoutActivity extends Activity {
    private static final String TAG = "Video2LayoutActivity";

    private String mVideoPath;
    private String videoPath2;
    private DrawPadView drawPadView;

    private MediaPlayer mplayer1 = null;
    private MediaPlayer mplayer2 = null;

    private boolean mplayerReady = false;
    private boolean mplayer2Ready = false;

    private VideoLayer videoLayer1 = null;
    private VideoLayer videoLayer2 = null;

    private String editTmpPath = null;
    private String dstPath = null;
    private LinearLayout playVideo;
    private MediaInfo mInfo;
    private TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videopicture_layout);
        initView();

        mVideoPath = getIntent().getStringExtra("videopath");
        drawPadView = (DrawPadView) findViewById(R.id.id_videopicture_drawpadview);


        /**
         * 在手机的默认路径下创建一个文件名,
         * 用来保存生成的视频文件,(在onDestroy中删除)
         */
        editTmpPath = SDKFileUtils.newMp4PathInBox();
        dstPath = SDKFileUtils.newMp4PathInBox();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startPlayVideo12();
            }
        }, 200);
    }

    /**
     * 把视频1,2 都准备好.
     */
    private void startPlayVideo12() {
        videoPath2 = CopyFileFromAssets.copyAssets(getApplicationContext(), "ping25s.mp4");

        if (mVideoPath != null && videoPath2 != null) {
            mplayer1 = new MediaPlayer();
            mplayer2 = new MediaPlayer();
            try {
                mplayer1.setDataSource(mVideoPath);
                mplayer2.setDataSource(videoPath2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mplayer1.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mplayerReady = true;
                    initDrawPad();
                }
            });
            mplayer2.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mplayer2Ready = true;
                    initDrawPad();
                }
            });
            mplayer1.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopDrawPad();
                }
            });
            mplayer1.prepareAsync();
            mplayer2.prepareAsync();
        } else {
            finish();
            return;
        }
    }

    /**
     * Step1:  init Drawpad  初始化DrawPad
     */
    private void initDrawPad() {
        if (mplayerReady && mplayer2Ready) {
            mInfo = new MediaInfo(mVideoPath, false);
            if (mInfo.prepare()) {
                drawPadView.setUpdateMode(DrawPadUpdateMode.ALL_VIDEO_READY, 25);

                drawPadView.setRealEncodeEnable(480, 480, 1200000, (int) mInfo.vFrameRate, editTmpPath);

                drawPadView.setDrawPadSize(480, 480, new onDrawPadSizeChangedListener() {

                    @Override
                    public void onSizeChanged(int viewWidth, int viewHeight) {
                        startDrawPad();
                    }
                });
            }
        }

    }

    /**
     * Step2:  start DrawPad 开始运行这个容器.
     */
    private void startDrawPad() {
        drawPadView.pauseDrawPad();  //先标志线程在开启后,暂停.
        if (drawPadView.startDrawPad()) {

            /**
             * 开始增加视频图层.
             */
            BitmapLayer layer = drawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
            layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight());  //增加一个背景,填充整个屏幕.

            //增加一个主视频.
            videoLayer1 = drawPadView.addMainVideoLayer(mplayer1.getVideoWidth(), mplayer1.getVideoHeight(), null);
            if (videoLayer1 != null) {
                mplayer1.setSurface(new Surface(videoLayer1.getVideoTexture()));
                mplayer1.start();
            }

            //增加另一个视频.
            videoLayer2 = drawPadView.addVideoLayer(mplayer2.getVideoWidth(), mplayer2.getVideoHeight(), null);
            mplayer2.setSurface(new Surface(videoLayer2.getVideoTexture()));  //视频
            mplayer2.start();
            mplayer2.setVolume(0.0f, 0.0f);

            drawPadView.resumeDrawPad();


            //对两个视频布局一下.
            videoLayer1.setScale(0.5f, 1.0f); //因为宽度缩放一半,高度没有缩放, 会变形一点.
            videoLayer1.setPosition(videoLayer1.getPadWidth() / 4, videoLayer1.getPositionY());


            videoLayer2.setScale(0.5f, 1.0f); //因为宽度缩放一半,高度没有缩放, 会变形一点.
            videoLayer2.setPosition(videoLayer2.getPadWidth() * 3 / 4, videoLayer2.getPositionY());
        }
    }

    /**
     * Step3 第三步: 停止运行DrawPad
     */
    private void stopDrawPad() {
        if (drawPadView != null && drawPadView.isRunning()) {
            drawPadView.stopDrawPad();

            if (mplayer2 != null) {
                mplayer2.stop();
                mplayer2.release();
                mplayer2 = null;
            }
            Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();

            if (SDKFileUtils.fileExist(editTmpPath)) {
                boolean ret = VideoEditor.encoderAddAudio(mVideoPath, editTmpPath, SDKDir.TMP_DIR, dstPath);
                if (!ret) {
                    dstPath = editTmpPath;
                } else {
                    SDKFileUtils.deleteFile(editTmpPath);
                }
                playVideo.setVisibility(View.VISIBLE);
            }
        }
    }

    boolean isFirstRemove = false;

    boolean isDestorying = false;  //是否正在销毁, 因为销毁会停止DrawPad

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isDestorying = true;
        if (mplayer1 != null) {
            mplayer1.stop();
            mplayer1.release();
            mplayer1 = null;
        }

        if (drawPadView != null) {
            drawPadView.stopDrawPad();
            drawPadView = null;
        }
        SDKFileUtils.deleteFile(dstPath);
        SDKFileUtils.deleteFile(editTmpPath);
    }


    private void initView() {
        tvHint = (TextView) findViewById(R.id.id_videopicture_hint);
        tvHint.setText(R.string.vdieo2_layout);

        playVideo = (LinearLayout) findViewById(R.id.id_videopicture_saveplay);
        playVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (SDKFileUtils.fileExist(dstPath)) {
                    Intent intent = new Intent(Video2LayoutActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("videopath", dstPath);
                    startActivity(intent);
                } else {
                    Toast.makeText(Video2LayoutActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
                }
            }
        });
        playVideo.setVisibility(View.GONE);
    }

}
