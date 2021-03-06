package com.example.advanceDemo;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.IF1977Filter;

import com.example.advanceDemo.view.FilterDemoAdapter;
import com.example.advanceDemo.view.HorizontalListView;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapGetFilters;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadSnapShotListener;
import com.lansosdk.box.onGetFiltersOutFrameListener;
import com.lansosdk.videoeditor.AVDecoder;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.DrawPadVideoExecute;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.FilterLibrary;
import com.lansosdk.videoeditor.FilterList;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.VideoOneDo;
import com.lansosdk.videoeditor.onVideoOneDoCompletedListener;
import com.lansosdk.videoeditor.onVideoOneDoProgressListener;
import com.lansosdk.videoeditor.FilterLibrary.FilterAdjuster;
import com.lansosdk.videoeditor.FilterLibrary.FilterType;
import com.lansosdk.videoeditor.FilterLibrary.OnGpuImageFilterChosenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Demo3LayerFilterActivity extends Activity {
    private static final String TAG = "Demo3LayerFilterActivity";

    private String mVideoPath;

    private DrawPadView drawPadView;

    private MediaPlayer mplayer = null;

    private VideoLayer filterLayer = null;

    private MediaInfo mInfo;

    private SeekBar skbarFilterAdjuster;
    /**
     * 在进行视频滤镜的过程中, 实时保存的视频画面文件的临时路径.
     */
    private String editTmpPath = null;
    /**
     * 滤镜处理后, 增加上原来音频文件的最终路径.
     */
    private String dstPath = null;

    private FilterDemoAdapter listAdapter;
    private HorizontalListView listFilterView;
    private FilterList filterList = new FilterList();
    /**
     * 您可以记录下当前操作的滤镜类, 然后在后台执行.
     */
    private String currrentFilterName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filter_layer_demo_layout);


        initView();

        mVideoPath = getIntent().getStringExtra("videopath");

        editTmpPath = SDKFileUtils.newMp4PathInBox();
        dstPath = SDKFileUtils.newMp4PathInBox();

        mInfo = new MediaInfo(mVideoPath, false);
        if (mInfo.prepare()) {
            new GetBitmapFiltersTask(mVideoPath).execute();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPlayVideo();
                }
            }, 100);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mplayer != null) {
            mplayer.stop();
            mplayer.release();
            mplayer = null;
        }
        if (videoOneDo != null) {
            videoOneDo.release();
            videoOneDo = null;
        }
    }

    private FilterAdjuster mFilterAdjuster;

    private void startPlayVideo() {
        mplayer = new MediaPlayer();
        try {
            mplayer.setDataSource(mVideoPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        mplayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                initDrawPad();
            }
        });
        mplayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                if (drawPadView != null && drawPadView.isRunning()) {
                    drawPadView.stopDrawPad();
                    findViewById(R.id.id_filterdemo_saveplay).setVisibility(View.VISIBLE);
                }
            }
        });
        mplayer.prepareAsync();
    }

    /**
     * Step1:开始 DrawPad 容器
     */
    private void initDrawPad() {
        MediaInfo info = new MediaInfo(mVideoPath);
        if (info.prepare()) {
            drawPadView.setUpdateMode(DrawPadUpdateMode.ALL_VIDEO_READY, 25);
            drawPadView.setRealEncodeEnable(480, 480, 1000000, (int) info.vFrameRate, editTmpPath);
            drawPadView.setOnDrawPadCompletedListener(new DrawPadCompleted());

            drawPadView.setDrawPadSize(480, 480, new onDrawPadSizeChangedListener() {

                @Override
                public void onSizeChanged(int viewWidth, int viewHeight) {
                    startDrawPad();
                }
            });
        }
    }

    /**
     * Step2: startDrawPad
     */
    private void startDrawPad() {
        if (drawPadView.startDrawPad()) {
            addVideoLayer();
        }
    }

    private void addVideoLayer() {
        //增加一个背景.
        BitmapLayer layer = drawPadView.addBitmapLayer(BitmapFactory.decodeResource(getResources(), R.drawable.videobg));
        layer.setScaledValue(layer.getPadWidth(), layer.getPadHeight());

        /**
         * 这里增加一个addVideoLayer, 并把设置滤镜效果为GPUImageSepiaFilter滤镜.
         */
        filterLayer = drawPadView.addMainVideoLayer(mplayer.getVideoWidth(), mplayer.getVideoHeight(), null);

        if (filterLayer != null) {
            mplayer.setSurface(new Surface(filterLayer.getVideoTexture()));
            mplayer.start();

        }
    }

    /**
     * DrawPad完成后的回调.
     *
     * @author Administrator
     */
    private class DrawPadCompleted implements onDrawPadCompletedListener {
        @Override
        public void onCompleted(DrawPad v) {
            if (isDestorying == false) {
                Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
                if (SDKFileUtils.fileExist(editTmpPath)) {
                    boolean ret = VideoEditor.encoderAddAudio(mVideoPath, editTmpPath, SDKDir.TMP_DIR, dstPath);
                    if (!ret) {
                        dstPath = editTmpPath;
                    } else {
                        SDKFileUtils.deleteFile(editTmpPath);
                    }
                }
            }
        }
    }

    private void initView() {
        findViewById(R.id.id_filterLayer_demo_next).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mplayer != null) {
                    mplayer.stop();
                    mplayer.release();
                    mplayer = null;
                }
                drawPadView.stopDrawPad();
                filterExecute();
            }
        });


        listFilterView = (HorizontalListView) findViewById(R.id.id_filterlayer_filterlist);
        listFilterView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if (filterLayer != null) {
                    GPUImageFilter filter = filterList.getFilter(getApplicationContext(), arg2);

                    currrentFilterName = filterList.getName(arg2);
                    filterLayer.switchFilterTo(filter);
                }
            }
        });

        drawPadView = (DrawPadView) findViewById(R.id.id_filterLayer_demo_view);
        skbarFilterAdjuster = (SeekBar) findViewById(R.id.id_filterLayer_demo_seek1);
        skbarFilterAdjuster.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (mFilterAdjuster != null) {
                    mFilterAdjuster.adjust(progress);
                }
            }
        });


        skbarFilterAdjuster.setMax(100);
        findViewById(R.id.id_filterLayer_demo_selectbtn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectFilter();
            }
        });


        findViewById(R.id.id_filterdemo_saveplay).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SDKFileUtils.fileExist(dstPath)) {
                    Intent intent = new Intent(Demo3LayerFilterActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("videopath", dstPath);
                    startActivity(intent);
                } else {
                    Toast.makeText(Demo3LayerFilterActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.id_filterdemo_saveplay).setVisibility(View.GONE);
    }

    /**
     * 选择滤镜效果,
     */
    private void selectFilter() {
        FilterLibrary.showDialog(this, new OnGpuImageFilterChosenListener() {

            @Override
            public void onGpuImageFilterChosenListener(final GPUImageFilter filter, String name) {
                if (filterLayer != null) {

                    currrentFilterName = name;
                    filterLayer.switchFilterTo(filter);
                    mFilterAdjuster = new FilterAdjuster(filter);
                    //如果这个滤镜 可调, 显示可调节进度条.
                    findViewById(R.id.id_filterLayer_demo_seek1).setVisibility(
                            mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
                }
            }
        });
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

        SDKFileUtils.deleteFile(dstPath);
        SDKFileUtils.deleteFile(editTmpPath);
    }
    //-----------------获取第一张图片, 并获取所有图片滤镜的异步执行------------------------

    public class GetBitmapFiltersTask extends AsyncTask<Object, Object, Boolean> {
        private String video;

        public GetBitmapFiltersTask(String videoPath) {
            video = videoPath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected synchronized Boolean doInBackground(Object... params) {
            getFirstFrame(video);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (filterList.isAllFilterBitmap()) {
                listAdapter = new FilterDemoAdapter(Demo3LayerFilterActivity.this, filterList);
                listAdapter.notifyDataSetChanged();
                listFilterView.setAdapter(listAdapter);
            }
        }
    }

    /**
     * 先获取第一帧, 然后根据第一帧去获取所有滤镜.
     *
     * @param src
     * @return
     */
    public boolean getFirstFrame(String src) {
        long decoderHandler = 0;
        IntBuffer mGLRgbBuffer;
        MediaInfo info = new MediaInfo(src, false);
        if (info.prepare()) {
            decoderHandler = AVDecoder.decoderInit(src);
            if (decoderHandler != 0) {
                mGLRgbBuffer = IntBuffer.allocate(info.vWidth * info.vHeight);
                mGLRgbBuffer.position(0);
                AVDecoder.decoderFrame(decoderHandler, -1, mGLRgbBuffer.array());
                AVDecoder.decoderRelease(decoderHandler);

                //转换为bitmap
                Bitmap bmp = Bitmap.createBitmap(info.vWidth, info.vHeight, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(mGLRgbBuffer);
                decoderHandler = 0;

                //拿到图片, 去获取多个滤镜.
                getBitmapFilters(bmp, info.vRotateAngle);

                return true;
            }
        }
        return false;
    }

    private void getBitmapFilters(Bitmap bmp, float angle) {
        filterList.addFilter("无", FilterType.NONE);
        filterList.addFilter("美颜", FilterType.BEAUTIFUL);
        filterList.addFilter("1AMARO", FilterType.AMARO);
        filterList.addFilter("2RISE", FilterType.RISE);
        filterList.addFilter("3HUDSON", FilterType.HUDSON);
        filterList.addFilter("4XPROII", FilterType.XPROII);
        filterList.addFilter("5SIERRA", FilterType.SIERRA);
        filterList.addFilter("6LOMOFI", FilterType.LOMOFI);
        filterList.addFilter("7EARLYBIRD", FilterType.EARLYBIRD);
        filterList.addFilter("8SUTRO", FilterType.SUTRO);
        filterList.addFilter("9TOASTER", FilterType.TOASTER);
        filterList.addFilter("10BRANNAN", FilterType.BRANNAN);
        filterList.addFilter("11INKWELL", FilterType.INKWELL);
        filterList.addFilter("12WALDEN", FilterType.WALDEN);
        filterList.addFilter("13HEFE", FilterType.HEFE);
        filterList.addFilter("14VALENCIA", FilterType.VALENCIA);
        filterList.addFilter("15NASHVILLE", FilterType.NASHVILLE);
        filterList.addFilter("16if1977", FilterType.IF1977);
        filterList.addFilter("17LORDKELVIN", FilterType.LORDKELVIN);


        BitmapGetFilters getFilter = new BitmapGetFilters(getApplicationContext(), bmp, filterList.getFilters(getApplicationContext()));
        if (bmp.getWidth() * bmp.getHeight() > 480 * 480) {  //如果图片太大了,则把滤镜后的图片缩小一倍输出.
            getFilter.setScaleWH(bmp.getWidth() / 2, bmp.getHeight() / 2);
        }
        getFilter.setRorate(angle);

        getFilter.setDrawpadOutFrameListener(new onGetFiltersOutFrameListener() {
            @Override
            public void onOutFrame(BitmapGetFilters v, Object obj) {
                Bitmap bmp2 = (Bitmap) obj;

                Log.i(TAG, "获取到的图片宽高:" + bmp2.getWidth() + " x " + bmp2.getHeight());

                filterList.addBitmap(bmp2);
            }
        });
        getFilter.start();//开始线程.

        getFilter.waitForFinish();//等待执行完毕, 您也可以不用等待,用[完成监听]来判断是否结束.
    }

    //-------------------------------------------------后台执行.
    private ProgressDialog mProgressDialog;
    private VideoOneDo videoOneDo;

    private void filterExecute() {
        mProgressDialog = new ProgressDialog(Demo3LayerFilterActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("正在后台处理:");
        mProgressDialog.setCancelable(false);

        videoOneDo = new VideoOneDo(getApplicationContext(), mVideoPath);

        GPUImageFilter filter = FilterLibrary.getFilterList().getFilter(getApplicationContext(), currrentFilterName);
        videoOneDo.setFilter(filter);
        videoOneDo.setOnVideoOneDoProgressListener(new onVideoOneDoProgressListener() {

            @Override
            public void onProgress(VideoOneDo v, float percent) {
                if (mProgressDialog != null) {
                    percent = percent * 100;
                    mProgressDialog.setMessage("正在后台处理:" + percent + " %");
                }
            }
        });
        videoOneDo.setOnVideoOneDoCompletedListener(new onVideoOneDoCompletedListener() {

            @Override
            public void onCompleted(VideoOneDo v, String dstVideo) {

                if (mProgressDialog != null) {
                    mProgressDialog.cancel();
                    mProgressDialog = null;
                }
                dstPath = dstVideo;
                findViewById(R.id.id_filterdemo_saveplay).setVisibility(View.VISIBLE);
                videoOneDo.release();
                videoOneDo = null;
            }
        });
        if (videoOneDo.start()) {
            mProgressDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "后台运行失败,请查看打印信息", Toast.LENGTH_SHORT).show();
        }
    }

}
