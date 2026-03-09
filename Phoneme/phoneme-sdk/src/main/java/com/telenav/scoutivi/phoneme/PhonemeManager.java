package com.telenav.scoutivi.phoneme;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.telenav.scoutivi.tts.phoneme.aidl.IPhonemeInterface;
import java.util.Collections;
import java.util.List;

public class PhonemeManager {
    private static final String TAG = "PhonemeManager";
    private static final long RECONNECT_DELAY_MS = 10000L;

    private static class Holder {
        @SuppressLint("StaticFieldLeak")
        private static final PhonemeManager INSTANCE = new PhonemeManager();
    }

    public static PhonemeManager get() {
        return Holder.INSTANCE;
    }

    private volatile IPhonemeInterface mStub = null;
    private volatile boolean mIsServiceBound = false;
    private volatile int mReconnectAttempts = 0;
    private volatile boolean mIsReconnecting = false;
    private Handler mHandler;
    private Context mContext;

    private PhonemeManager() {
        // Private constructor
    }

    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, System.currentTimeMillis() + ": Remote service process died (DeathRecipient)");
            mIsServiceBound = false;
            mStub = null;
            scheduleReconnect();
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 注册监听
            if (service != null) {
                try {
                    service.linkToDeath(mDeathRecipient, 0);
                } catch (Exception e) {
                    Log.e(TAG, "linkToDeath failed", e);
                }
            }
            mStub = IPhonemeInterface.Stub.asInterface(service);
            mIsServiceBound = true;
            mReconnectAttempts = 0;
            mIsReconnecting = false;
            Log.v(TAG, System.currentTimeMillis() + ": Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
            mStub = null;
            Log.v(TAG, System.currentTimeMillis() + ": Service disconnected");
            scheduleReconnect();
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.w(TAG, System.currentTimeMillis() + ": onBindingDied");
            mIsServiceBound = false;
            mStub = null;
            scheduleReconnect();
        }
    };

    private final Runnable mReconnectRunnable = new Runnable() {
        @Override
        public void run() {
            mIsReconnecting = false;
            connect();
        }
    };

    public void init(Context context) {
        mHandler = new Handler(Looper.getMainLooper());
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        connect();
    }

    private void connect() {
        Context context = mContext;
        if (context == null) {
            Log.e(TAG, System.currentTimeMillis() + ": Context is null, cannot connect Service");
            return;
        }

        Intent intent = new Intent();
        intent.setPackage("com.telenav.scoutivi.espeak");
        intent.setAction("com.telenav.scoutivi.tts.PHONEME_SERVICE");

        // 绑定 Service
        if (mIsServiceBound) return;
        boolean bound = context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.e(TAG, System.currentTimeMillis() + ": bindService failed, check APK/Service declaration");
            scheduleReconnect();
        } else {
            Log.v(TAG, System.currentTimeMillis() + ": bindService success");
        }
    }

    public void scheduleReconnect() {
        if (mIsReconnecting) {
            Log.v(TAG, System.currentTimeMillis() + ": Already reconnecting, skip");
            return;
        }
        mIsReconnecting = true;
        mReconnectAttempts++;
        if (mHandler != null) {
            mHandler.removeCallbacks(mReconnectRunnable);
            Log.v(TAG, System.currentTimeMillis() + ": Scheduling reconnect attempt " + mReconnectAttempts + " in " + RECONNECT_DELAY_MS + " ms");
            mHandler.postDelayed(mReconnectRunnable, RECONNECT_DELAY_MS);
        }
    }

    public boolean isConnected() {
        return mIsServiceBound && mStub != null;
    }

    public List<List<String>> phoneme(String input, String espeakVoice) {
        IPhonemeInterface stub = mStub;
        if (!isConnected() || stub == null) {
            Log.e(TAG, System.currentTimeMillis() + ": Service not bound, cannot phoneme, try reconnect");
            scheduleReconnect();
            return Collections.emptyList();
        }
        try {
            return stub.phoneme(input, espeakVoice).getData();
        } catch (DeadObjectException e) {
            Log.e(TAG, System.currentTimeMillis() + ": phoneme DeadObjectException", e);
            mIsServiceBound = false;
            mStub = null;
            scheduleReconnect();
            return Collections.emptyList();
        } catch (Exception e) {
            Log.e(TAG, System.currentTimeMillis() + ": phoneme error", e);
            return Collections.emptyList();
        }
    }

    public String tashkeelRun(String input) {
        IPhonemeInterface stub = mStub;
        if (!isConnected() || stub == null) {
            Log.e(TAG, System.currentTimeMillis() + ": Service not bound, cannot tashkeelRun, try reconnect");
            scheduleReconnect();
            return "";
        }
        try {
            return stub.tashkeelRun(input);
        } catch (DeadObjectException e) {
            Log.e(TAG, System.currentTimeMillis() + ": tashkeelRun DeadObjectException", e);
            mIsServiceBound = false;
            mStub = null;
            scheduleReconnect();
            return "";
        } catch (Exception e) {
            Log.e(TAG, System.currentTimeMillis() + ": tashkeelRun error", e);
            return "";
        }
    }

    public boolean isSaveAudio() {
        IPhonemeInterface stub = mStub;
        if (!isConnected() || stub == null) {
            Log.e(TAG, System.currentTimeMillis() + ": Service not bound, cannot isSaveAudio, try reconnect");
            scheduleReconnect();
            return false;
        }
        try {
            return stub.getConfig().isSaveAudio();
        } catch (DeadObjectException e) {
            Log.e(TAG, System.currentTimeMillis() + ": isSaveAudio DeadObjectException", e);
            mIsServiceBound = false;
            mStub = null;
            scheduleReconnect();
            return false;
        } catch (Exception e) {
            Log.e(TAG, System.currentTimeMillis() + ": isSaveAudio error", e);
            return false;
        }
    }

    public void release() {
        if (mIsServiceBound && mContext != null) {
            try {
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                mContext.unbindService(mServiceConnection);
                Log.v(TAG, System.currentTimeMillis() + ": Service released");
            } catch (Exception e) {
                Log.e(TAG, System.currentTimeMillis() + ": release error", e);
            } finally {
                mStub = null;
                mIsServiceBound = false;
                mReconnectAttempts = 0;
            }
        }
    }
}

