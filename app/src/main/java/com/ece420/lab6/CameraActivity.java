package com.ece420.lab6;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
// import android.util.Log;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
// import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    // UI Variable
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder surfaceHolder2;
    private TextView textHelper;
    // Camera Variable
    private Camera camera;
    boolean previewing = false;
    private int width = 640;
    private int height = 480;
    // Kernels
    private double[][] kernelS = new double[][] {{-1,-1,-1},{-1,9,-1},{-1,-1,-1}};
    private double[][] kernelX = new double[][] {{1,0,-1},{1,0,-1},{1,0,-1}};
    private double[][] kernelY = new double[][] {{1,1,1},{0,0,0},{-1,-1,-1}};
    byte[] rawdata;

    private Button snap_button;
    public static int snapFlag = 0;

    private Paint paint;
    private PointF startPoint, endPoint;
    private boolean isDrawing;

    public float touchX;
    public float touchY;

    public boolean pressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        super.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Modify UI Text
        textHelper = (TextView) findViewById(R.id.Helper);
        if(MainActivity.appFlag == 1) textHelper.setText("Taken Photo");
        else if(MainActivity.appFlag == 2) textHelper.setText("Sharpened Image");
        else if(MainActivity.appFlag == 3) textHelper.setText("Edge Detected Image");

        // Setup Surface View handler
        surfaceView = (SurfaceView)findViewById(R.id.ViewOrigin);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView2 = (SurfaceView)findViewById(R.id.ViewHisteq);
        surfaceView2.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                touchX = event.getX();
                touchY = event.getY();
                System.out.println(touchX);
                System.out.println(touchY);
                System.out.println("\n");
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pressed = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    pressed = false;
                }
                return true;
            }
        });
        surfaceHolder2 = surfaceView2.getHolder();



        // Setup Button for Edge Detection
        snap_button = (Button) findViewById(R.id.snap_button);
        snap_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapFlag = ~snapFlag;

            }
        });

        paint = new Paint();

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Must have to override native method

        return;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(!previewing) {
            camera = Camera.open();
            if (camera != null) {
                try {
                    // Modify Camera Settings
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(width, height);
                    // Following lines could log possible camera resolutions, including
                    // 2592x1944;1920x1080;1440x1080;1280x720;640x480;352x288;320x240;176x144;
                    // List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                    // for(int i=0; i<sizes.size(); i++) {
                    //     int height = sizes.get(i).height;
                    //     int width = sizes.get(i).width;
                    //     Log.d("size: ", Integer.toString(width) + ";" + Integer.toString(height));
                    // }
                    camera.setParameters(parameters);
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.setPreviewCallback(new PreviewCallback() {
                        public void onPreviewFrame(byte[] data, Camera camera)
                        {
                            // Lock canvas
                            Canvas canvas = surfaceHolder2.lockCanvas(null);
                            // Where Callback Happens, camera preview frame ready

                            onCameraFrame(canvas,data);
                            drawingMethod(canvas);

                            // Unlock canvas
                            surfaceHolder2.unlockCanvasAndPost(canvas);
                        }
                    });
                    camera.startPreview();
                    previewing = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Cleaning Up
        if (camera != null && previewing) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            previewing = false;
        }
    }



//    private void Draw(SurfaceHolder holder) {
//        Canvas canvas = holder.lockCanvas();
//        drawingMethod(canvas);
//        holder.unlockCanvasAndPost(canvas);
//    }

    private void drawingMethod(Canvas canvas) {
        byte tmp;
        if (pressed == true) {
            int y, x;
            y = (int) (touchY);
            x = (int) (touchX);
            for (y = (int) touchY-5; y < (int) touchY+5; y++) {
                for (x = (int) touchX-5; x < (int) touchX+5; x++) {
                    if (x>=0 & x<height & y>=0 & y<width) {
                        rawdata[(height-y) * width + (width-x)] = (byte) (rawdata[(height-y) * width + (width-x)] | 0x00FF);
//
                    }
                }
            }
        }
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(Color.RED);
//            canvas.drawCircle(touchX, touchY, 20, paint);
//            canvas.drawCircle(0, 0, 20, paint);
//        }

    }



    // Camera Preview Frame Callback Function
    protected void onCameraFrame(Canvas canvas, byte[] data) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        int retData[] = new int[width * height];

        // Apply different processing methods
//        if(MainActivity.appFlag == 1){
//            byte[] histeqData = histEq(data, width, height);
//            retData = yuv2rgb(histeqData);
//        }
//        else if (MainActivity.appFlag == 2){
//
//            int[] sharpData = conv2(data, width, height, kernelS);
//            retData = merge(sharpData, sharpData);
//        }
//        else if (MainActivity.appFlag == 3){
//            int[] xData = conv2(data, width, height, kernelX);
//            int[] yData = conv2(data, width, height, kernelY);
//            retData = merge(xData, yData);
//        }

        if (snapFlag == 0){
            rawdata = data.clone();
        } else {
            retData = yuv2rgb(rawdata);
        }

        byte[] segmented = segment(rawdata, width, height);
        retData = yuv2rgb(segmented);

        // Create ARGB Image, rotate and draw
        Bitmap bmp = Bitmap.createBitmap(retData, width, height, Bitmap.Config.ARGB_8888);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        canvas.drawBitmap(bmp, new Rect(0,0, height, width), new Rect(0,0, canvas.getWidth(), canvas.getHeight()),null);
    }

    // Helper function to convert YUV to RGB
    public int[] yuv2rgb(byte[] data){
        final int frameSize = width * height;
        int[] rgb = new int[frameSize];

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) data[yp])) - 16;
                y = y<0? 0:y;

                if ((i & 1) == 0) {
                    v = (0xff & data[uvp++]) - 128;
                    u = (0xff & data[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                r = r<0? 0:r;
                r = r>262143? 262143:r;
                g = g<0? 0:g;
                g = g>262143? 262143:g;
                b = b<0? 0:b;
                b = b>262143? 262143:b;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    // Helper function to merge the results and convert GrayScale to RGB
    public int[] merge(int[] xdata,int[] ydata){
        int size = height * width;
        int[] mergeData = new int[size];
        for(int i=0; i<size; i++)
        {
            int p = (int)Math.sqrt((xdata[i] * xdata[i] + ydata[i] * ydata[i]) / 2);
            mergeData[i] = 0xff000000 | p<<16 | p<<8 | p;
        }
        return mergeData;
    }

    // Function for Histogram Equalization
    public byte[] histEq(byte[] data, int width, int height){
        byte[] histeqData = new byte[data.length];
        int size = height * width;

        // Perform Histogram Equalization
        // Note that you only need to manipulate data[0:size] that corresponds to luminance
        // The rest data[size:data.length] is for colorness that we handle for you
        // *********************** START YOUR CODE HERE  **************************** //
//        int[] hs = new int[256];
//        for (int i = 0; i < height; i++){
//            for (int j = 0; j < width; j++){
//                hs[Math.abs(data[i*width+j])] += 1;
//            }
//        }
//        int[] cdf = new int[256];
//        int sum = 0;
//        for (int i = 0; i < 256; i++){
//            sum += hs[i];
//            cdf[i] = sum;
//        }
//        int cmin = 0;
//        cmin = cdf[0];
//        for (int r = 0; r < height; r++){
//            for (int c = 0; c < width; c++){
//                int tmp = Math.round(255*(cdf[Math.abs(data[r*width+c])]-cmin)/(size-cmin));
//                histeqData[r*width+c] = (byte) (tmp & 0x00FF);
//            }
//        }

        int[] histogram = new int[256];

        for (int i = 0; i < size; ++i) {
            int dataVal = data[i] & 0x00FF;
            if (dataVal > 255) System.out.println(dataVal);
            histogram[dataVal]++;
        }

        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i-1] + histogram[i];
        }

        // Normalize cdf
        int cdf_min = 256;
        for (int i = 0; i < 256; ++i) {
            if (cdf[i] < cdf_min && cdf[i] != 0) {
                cdf_min = cdf[i];
            }
        }
        for (int i =0; i < 256; ++i) {
            cdf[i] = (int) Math.round( (cdf[i] - cdf_min)*255.0/(size - cdf_min));
        }

        // Apply cdf
        for (int i = 0; i < size; ++i) {
            int dataVal = data[i] & 0x00FF;
            histeqData[i] = (byte) cdf[dataVal];
        }

        // *********************** End YOUR CODE HERE  **************************** //
        // We copy the colorness part for you, do not modify if you want rgb images
        for(int i=size; i<data.length; i++){
            histeqData[i] = data[i];
        }
        return histeqData;
    }

    public int[] conv2(byte[] data, int width, int height, double kernel[][]){
        // 0 is black and 255 is white.
        int size = height * width;
        int[] convData = new int[size];

        // Perform single channel 2D Convolution
        // Note that you only need to manipulate data[0:size] that corresponds to luminance
        // The rest data[size:data.length] is ignored since we only want grayscale output
        // *********************** START YOUR CODE HERE  **************************** //
        int kernel_height = kernel.length;
        int kernel_width = kernel[0].length;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int outputIndex = y*width + x; // row-major index
                double convResult = 0;

                for (int i = -(kernel_height/2); i < kernel_height/2 + 1; ++i) {
                    for (int j = -(kernel_width/2); j < kernel_width/2 + 1; ++j) {
                        if (y+i >= 0 && y+i < height && x+j >= 0 && x+j < width) {
                            int flatIndex = (y+i)*width + x + j;
                            int dataVal = data[flatIndex] & 0x00FF;
                            convResult += dataVal*kernel[-i+1][-j+1];
                        }
                    }
                }
                convData[outputIndex] = (int) convResult;
            }
        }


        // *********************** End YOUR CODE HERE  **************************** //
        return convData;
    }

    public byte[] segment(byte[] data, int width, int height) {
        GraphSegmenter segmenter = new GraphSegmenter();
        List<IntPair> bkgSeeds, objSeeds;
        bkgSeeds = new ArrayList<IntPair>();
        objSeeds = new ArrayList<IntPair>();
        // Assume that the center is the object and the region on the sides are background

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                IntPair p = new IntPair(i,j);
                if (i >= width/2 - 30 && i <= width/2 + 30 && j >= height/2 - 30 && j <= height/2 + 30) {
                    objSeeds.add(p);
                } else if (i < 10 || i > width-10 || j < 10 || j > height - 10) {
                    bkgSeeds.add(p);
                }
            }
        }

        System.out.println(bkgSeeds);

        return segmenter.segmentImage(data, width, height, bkgSeeds, objSeeds);
    }

}
