package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import su.levenetc.android.textsurface.TextSurface;
import su.levenetc.android.textsurface.interfaces.IEndListener;
import su.levenetc.android.textsurface.interfaces.ISurfaceAnimation;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.view.LanSongLoveText;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.DrawPadView.onViewAvailable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 演示:  告白浪漫情诗 .
 * 流程: 在DrawPad容器上增加一个VieLayer图层 ,利用TextSurface这个文字动画效果的开源库, 绘制出浪漫的情诗文字.
 */
public class ViewLayerOnlyActivity extends Activity implements IEndListener {
    private static final String TAG = "ViewLayerOnlyActivity";

    private String audioPath;
    private MediaPlayer audioPlayer;
    private DrawPadView drawPadView;
    /**
     * 采用github上开源的文字动画类, 您可以从https://github.com/elevenetc/TextSurface下载源代码.
     * 当前也可以直接使用我们封装好的textsurface.jar库.
     */
    private TextSurface textSurface;
    private ViewLayerRelativeLayout mGLRelativeLayout;


    private String editorTmpPath = null;

    private String dstPath = null;
    private ViewLayer mViewLayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpen_preview_layout);


        drawPadView = (DrawPadView) findViewById(R.id.id_viewLayer_DrawPad_view);
        findViewById(R.id.id_viewLayer_saveplay).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SDKFileUtils.fileExist(dstPath)) {
                    Intent intent = new Intent(ViewLayerOnlyActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("videopath", dstPath);
                    startActivity(intent);
                } else {
                    Toast.makeText(ViewLayerOnlyActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.id_viewLayer_saveplay).setVisibility(View.GONE);

        mGLRelativeLayout = (ViewLayerRelativeLayout) findViewById(R.id.id_viewLayer_gllayout);
        textSurface = (TextSurface) findViewById(R.id.text_surface);
        /**
         * 在手机的默认路径下创建一个文件名,用来保存生成的视频文件,
         * (在onDestroy中删除)
         */
        editorTmpPath = SDKFileUtils.newMp4PathInBox();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                initDrawPad();
            }
        }, 200);
    }

    /**
     * Step1: 初始化 DrawPad 容器
     */
    private void initDrawPad() {
        //设置为自动刷新模式, 帧率为25
        drawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH, 25);
        //使能实时录制,并设置录制后视频的宽度和高度, 码率, 帧率,保存路径.//因为视频就是一些文字,没有剧烈的画面运动,这里把码率设置小一些为600k
        drawPadView.setRealEncodeEnable(480, 480, 1000 * 1000, (int) 25, editorTmpPath);

        drawPadView.setOnDrawPadCompletedListener(new DrawPadCompleted());

        //设置DrawPad的宽高, 这里设置为480x480,如果您已经在xml中固定大小,则不需要再次设置,
        //可以直接调用startDrawPad来开始录制.
        drawPadView.setDrawPadSize(480, 480, new onDrawPadSizeChangedListener() {

            @Override
            public void onSizeChanged(int viewWidth, int viewHeight) {
                startDrawPad();
            }
        });
    }

    /**
     * Step2: 开始运行 Drawpad线程.
     * <p>
     * (结束,是在playGaoBai()的时间到后, 手动stopDrawPad)
     */
    private void startDrawPad() {
        if (drawPadView.startDrawPad()) {
            playAudio();
            addViewLayer();
            playGaoBai();
        }
    }

    //增加一个ViewLayer到容器上.
    private void addViewLayer() {
        mViewLayer = drawPadView.addViewLayer();
        mGLRelativeLayout.bindViewLayer(mViewLayer);
        mGLRelativeLayout.invalidate();

        ViewGroup.LayoutParams params = mGLRelativeLayout.getLayoutParams();
        params.height = mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.

        mGLRelativeLayout.setLayoutParams(params);
    }

    //DrawPad完成时的回调.
    private class DrawPadCompleted implements onDrawPadCompletedListener {
        @Override
        public void onCompleted(DrawPad v) {
            if (isDestorying == false) {
                if (SDKFileUtils.fileExist(editorTmpPath)) {
                    //可以在这里利用VideoEditor.java类来增加声音等.
                    dstPath = SDKFileUtils.createMp4FileInBox();
                    audioPath = CopyFileFromAssets.copyAssets(getApplicationContext(), "thrid50s.m4a");
                    VideoEditor editor = new VideoEditor();
                    editor.executeVideoMergeAudio(editorTmpPath, audioPath, dstPath);
                    findViewById(R.id.id_viewLayer_saveplay).setVisibility(View.VISIBLE);
                }
                toastStop();
            }
        }
    }

    private void toastStop() {
        Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }

    boolean isDestorying = false;  //是否正在销毁, 因为销毁会停止DrawPad

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestorying = true;
        if (drawPadView != null) {
            drawPadView.stopDrawPad();
            drawPadView = null;
        }
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.release();
            audioPlayer = null;
        }

        SDKFileUtils.deleteFile(editorTmpPath);
        gaoBaiChapter = MAX_CHAPTER + 1; //不在让画面更新.
    }

    private void playAudio() {
        if (audioPlayer == null) {

            audioPath = CopyFileFromAssets.copyAssets(getApplicationContext(), "thrid50s.m4a");
            audioPlayer = new MediaPlayer();
            try {
                audioPlayer.setDataSource(audioPath);
                audioPlayer.prepare();
                audioPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //  /-----------------------------
    private int gaoBaiChapter = 0;
    private int MAX_CHAPTER = 6;

    private void playGaoBai() {
        gaoBaiChapter++;
        if (gaoBaiChapter > MAX_CHAPTER) {

            //TextSurface 先调用endlisnter,再开始绘制,这样最后一帧有可能没有绘制出来,这里演示折中一下.
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (drawPadView != null)
                        drawPadView.stopDrawPad();
                }
            }, 500);
            return;
        }
        textSurface.reset();
        switch (gaoBaiChapter) {
            case 1:
                LanSongLoveText.play(getApplicationContext(), textSurface, getAssets(), this);
                break;
            case 2:
                LanSongLoveText.play2(getApplicationContext(), textSurface, getAssets(), this);
                break;
            case 3:
                LanSongLoveText.play3(getApplicationContext(), textSurface, getAssets(), this);
                break;
            case 4:
                LanSongLoveText.play4(getApplicationContext(), textSurface, getAssets(), this);
                break;
            case 5:
                LanSongLoveText.play5(getApplicationContext(), textSurface, getAssets(), this);
                break;
            case 6:
                LanSongLoveText.play6(getApplicationContext(), textSurface, getAssets(), this);
                break;
            default:
                break;
        }
    }

    //su.levenetc.android.textsurface.interfaces.IEndListener的监听.
    @Override
    public void onAnimationEnd(ISurfaceAnimation animation) {
        playGaoBai();
    }
}
