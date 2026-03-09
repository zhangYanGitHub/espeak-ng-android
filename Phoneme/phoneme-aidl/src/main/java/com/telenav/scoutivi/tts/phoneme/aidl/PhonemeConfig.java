package com.telenav.scoutivi.tts.phoneme.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class PhonemeConfig implements Parcelable {
    private final boolean saveAudio;

    public PhonemeConfig(boolean saveAudio) {
        this.saveAudio = saveAudio;
    }

    protected PhonemeConfig(Parcel in) {
        saveAudio = in.readByte() != 0;
    }

    public static final Creator<PhonemeConfig> CREATOR = new Creator<PhonemeConfig>() {
        @Override
        public PhonemeConfig createFromParcel(Parcel in) {
            return new PhonemeConfig(in);
        }

        @Override
        public PhonemeConfig[] newArray(int size) {
            return new PhonemeConfig[size];
        }
    };

    public boolean isSaveAudio() {
        return saveAudio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (saveAudio ? 1 : 0));
    }
}

