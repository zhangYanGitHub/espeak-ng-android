// IPhonemeInterface.aidl
package com.telenav.scoutivi.tts.phoneme.aidl;
import com.telenav.scoutivi.tts.phoneme.aidl.PhonemeResult;
import com.telenav.scoutivi.tts.phoneme.aidl.PhonemeConfig;


interface IPhonemeInterface {

     String tashkeelRun(String input);

     PhonemeResult phoneme(String input,String espeakVoice);

     PhonemeConfig getConfig();
}
