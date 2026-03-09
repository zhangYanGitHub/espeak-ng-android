package com.telenav.scoutivi.tts.phoneme.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/7/30 15:27
 */
public class PhonemeResult implements Parcelable {
    private final List<List<String>> data;

    public PhonemeResult(List<List<String>> data) {
        this.data = data;
    }

    protected PhonemeResult(Parcel in) {
        // Deserialize List<List<String>>
        int size = in.readInt();
        if (size >= 0) {
            data = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                List<String> innerList = new ArrayList<>();
                in.readStringList(innerList);
                data.add(innerList);
            }
        } else {
            data = new ArrayList<>();
        }
    }

    public static final Creator<PhonemeResult> CREATOR = new Creator<PhonemeResult>() {
        @Override
        public PhonemeResult createFromParcel(Parcel in) {
            return new PhonemeResult(in);
        }

        @Override
        public PhonemeResult[] newArray(int size) {
            return new PhonemeResult[size];
        }
    };

    public List<List<String>> getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Serialize List<List<String>>
        if (data != null) {
            dest.writeInt(data.size());
            for (List<String> innerList : data) {
                dest.writeStringList(innerList);
            }
        } else {
            dest.writeInt(0);
        }
    }
}

