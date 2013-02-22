package com.ford.openxc.webcam;

import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class WebcamPreview extends SurfaceView implements SurfaceHolder.Callback,
        Runnable {

    private static String TAG = "WebcamPreview";

    private WebcamManager mWebcamManager;
    private boolean mRunning = true;
    private Context mContext;

    private SurfaceHolder mHolder;
    private Bitmap bmp=null;

    // This definition also exists in ImageProc.h.
    // Webcam must support the resolution 640x480 with YUYV format.
    static final int IMG_WIDTH = 640;
    static final int IMG_HEIGHT = 480;

    // The following variables are used to draw camera images.
    private int winWidth=0;
    private int winHeight=0;
    private Rect rect;
    private int dw, dh;
    private float rate;

    public WebcamPreview(Context context) {
        super(context);
        init(context);
    }

    public WebcamPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "WebcamPreview constructed");
        setFocusable(true);

        mContext = context;

        mHolder = getHolder();
        mHolder.addCallback(this);

        // TODO does this ever change, e.g. do we need to run it again in
        // surfaceChanged?
        winWidth = this.getWidth();
        winHeight = this.getHeight();

        if(winWidth * 3 / 4 <= winHeight) {
            dw = 0;
            dh = (winHeight - winWidth * 3 / 4) / 2;
            rate = ((float) winWidth) / IMG_WIDTH;
            rect = new Rect(dw, dh, dw + winWidth - 1,
                    dh + winWidth * 3 / 4 - 1);
        } else {
            dw = (winWidth - winHeight * 4 / 3) / 2;
            dh = 0;
            rate = ((float) winHeight) / IMG_HEIGHT;
            rect = new Rect(dw, dh, dw + winHeight * 4 / 3 - 1,
                    dh + winHeight - 1);
        }

        mContext.bindService(new Intent(mContext, WebcamManager.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void run() {
        while(mRunning) {
            Bitmap bitmap = mWebcamManager.getImage();
            Canvas canvas = mHolder.lockCanvas();
            if(canvas != null) {
                canvas.drawBitmap(bmp, null, rect, null);
                mHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        (new Thread(this)).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
        mRunning = false;

        if(mConnection != null) {
            Log.i(TAG, "Unbinding from webcam manager");
            mContext.unbindService(mConnection);
            mConnection = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d("WebCam", "surfaceChanged");
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to WebcamManager");
            mWebcamManager = ((WebcamManager.WebcamBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "WebcamManager disconnected unexpectedly");
            mWebcamManager = null;
        }
    };
}
