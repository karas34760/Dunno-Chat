package ChatApp.android;

import android.animation.ValueAnimator;
import android.app.Service;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;

import android.os.CountDownTimer;

import android.os.Handler;

import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.Toast;

import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationCompat;

import de.hdodenhof.circleimageview.CircleImageView;


public class FloatingWidgetService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private Point szWindow = new Point();
    private WindowManager.LayoutParams params;

    public boolean isLeft = true;
    public CircleImageView BubbleImage;
    public DisplayMetrics metrics;
    public int windowWidth;
    public ConstraintLayout constraintLayout;
    public ConstraintSet constraintSet = new ConstraintSet();
    public String receive_text;
    public TextView txtBubbleText;

    BroadcastReceiver broadcastReceiver;


    public FloatingWidgetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.bubble, null);
        //FrameLayout bubble = mFloatingView.findViewById(R.id.bubble_frame);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            mWindowManager.getDefaultDisplay().getSize(szWindow);
        } else {
            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            szWindow.set(width, height);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase("getting_data")) {
                    byte[] imgbyte = intent.getByteArrayExtra("byteArray");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgbyte, 0, imgbyte.length);
                    BubbleImage.setImageBitmap(bitmap);
                }
                if (action.equalsIgnoreCase("getting_text")) {
                    receive_text = intent.getStringExtra("receive_text");
                    txtBubbleText = mFloatingView.findViewById(R.id.txtchatpop);
                    txtBubbleText.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Hide your View after 3 seconds
                            txtBubbleText.setText(receive_text);
                            txtBubbleText.setVisibility(View.GONE);
                        }
                    }, 3000);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("getting_data");
        registerReceiver(broadcastReceiver, intentFilter);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //Set the close button
        ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window
                stopSelf();
            }
        });

        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int windowWidth = metrics.widthPixels;
            int windowHeight = metrics.heightPixels;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:

                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        if (Xdiff != 0 || Ydiff !=0 ) {
                            resetPosition((int) event.getRawX());
                        }
                        //
                        if (Math.abs(Xdiff) < 5 && Math.abs(Ydiff) < 5) {
                            Activity currentactivity = GlobalStuff.getCurrentActivity();
                            Intent intent = new Intent(getApplicationContext(), currentactivity.getClass());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("fromwhere","ser");
                            PendingIntent pendingIntent =
                                    PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            try {
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    case MotionEvent.ACTION_MOVE:

                        //Calculate the X and Y coordinates of the view.

                        Xdiff = (int) (event.getRawX() - initialTouchX);
                        resetMessagePosition((int) event.getRawX());
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    private void startMyOwnForeground() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String NOTIFICATION_CHANNEL_ID = "ChatApp.android";
            String channelName = "DunnoChat Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
    }

    private void moveToLeft() {
                ValueAnimator va = ValueAnimator.ofFloat(params.x, 0);
                int mDuration = 250;
                va.setDuration(mDuration);
                va.addUpdateListener(animation -> {
                    params.x = Math.round((Float) animation.getAnimatedValue());
                    mWindowManager.updateViewLayout(mFloatingView, params);
                });
                va.start();
    }

    private void moveToRight() {
        metrics = getResources().getDisplayMetrics();
        windowWidth = metrics.widthPixels;
        ValueAnimator va = ValueAnimator.ofFloat(params.x, windowWidth);
        int mDuration = 250;
        va.setDuration(mDuration);
        va.addUpdateListener(animation -> {
            params.x = Math.round((Float) animation.getAnimatedValue());
            mWindowManager.updateViewLayout(mFloatingView, params);
        });
        va.start();
    }

    private void resetPosition(int x_cord_now) {
        metrics = getResources().getDisplayMetrics();
        windowWidth = metrics.widthPixels;
        if (x_cord_now <= szWindow.x / 2) {
                isLeft = true;
                moveToLeft();
            } else {
                isLeft = false;
                moveToRight();
            }

    }

    private void resetMessagePosition(int x_cord_now) {
        metrics = getResources().getDisplayMetrics();
        constraintLayout = mFloatingView.findViewById(R.id.root_container);
        constraintSet.clone(constraintLayout);
        windowWidth = metrics.widthPixels;
        constraintSet.clear(R.id.txtchatpop,ConstraintSet.START);
        constraintSet.clear(R.id.txtchatpop,ConstraintSet.END);
        constraintSet.clear(R.id.bubble_img,ConstraintSet.END);
        if (x_cord_now <= windowWidth / 2) {
            isLeft = true;
            constraintSet.connect(R.id.txtchatpop,ConstraintSet.START,R.id.bubble_img,ConstraintSet.END,5);
        } else {
            isLeft = false;
            constraintSet.connect(R.id.txtchatpop,ConstraintSet.END,R.id.bubble_img,ConstraintSet.START,5);
            constraintSet.connect(R.id.bubble_img,ConstraintSet.END,R.id.txtchatpop,ConstraintSet.START,5);
        }
        constraintSet.applyTo(constraintLayout);
    }

}
