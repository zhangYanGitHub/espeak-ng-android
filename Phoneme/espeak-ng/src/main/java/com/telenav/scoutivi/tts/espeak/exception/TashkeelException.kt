package com.telenav.scoutivi.tts.espeak.exception

/**
 * @Description: class role description
 * @Author: Yan
 * @Date: 2024/8/1 10:29
 *
 * Use Native Libs tashkeel_api.cpp (located src/lib/tashkeel)
 */
class TashkeelException(msg: String) : RuntimeException(msg) {
}