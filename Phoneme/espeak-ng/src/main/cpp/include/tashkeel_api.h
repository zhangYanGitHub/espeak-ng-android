//
// Created by Zhang, Yan (Yan) on 2024/7/30.
//

#ifndef SCOUTIVI_APP_TTS_TASHKEEL_API_H

#define SCOUTIVI_APP_TTS_TASHKEEL_API_H

#include "piper-phonemize/tashkeel.hpp"
#include "Log.h"
#include <unordered_set>
#include <locale>
#include <codecvt>
#include <stdexcept>

#define LOG_TAG "TASHKEEL"


// 常量定义
constexpr size_t CHAR_LIMIT = 12000;
constexpr char PAD = '_';
constexpr char NUMERAL_SYMBOL = '#';

// 使用 std::vector 初始化字符数组
const std::vector<char32_t> NUMERALS = {
        U'0', U'1', U'2', U'3', U'4', U'5', U'6', U'7', U'8', U'9',
        U'٠', U'١', U'٢', U'٣', U'٤', U'٥', U'٦', U'٧', U'٨', U'٩'
};

class TashkeelApi {
private:
    std::optional<float> taskeen_threshold;
    tashkeel::State m_state;
    JNIEnv *m_env;

    // 静态映射定义
    static const std::unordered_map<char32_t, int64_t> INPUT_ID_MAP; // 定义时不填充数据

    static const std::unordered_map<uint8_t, std::u32string> TARGET_ID_MAP; // 定义时不填充数据

    static const std::unordered_map<std::u32string, int64_t> HINT_ID_MAP; // 定义时不填充数据

    static const std::unordered_set<uint8_t> TARGET_META_CHAR_IDS; // 定义时不填充数据

    static const std::unordered_set<char32_t> ARABIC_DIACRITICS; // 定义时不填充数据

    static const std::unordered_map<std::u32string, std::u32string> NORMALIZED_DIAC_MAP;

    bool is_diacritic_char(char32_t c) const;

    std::pair<std::u32string, std::vector<std::u32string>> extract_chars_and_diacritics(
            const std::u32string &input_text,
            bool normalize_diacritics) const;

    std::pair<std::u32string, std::unordered_set<char32_t>> to_valid_chars(
            const std::u32string &input) const;

    std::pair<std::u32string, std::unordered_set<char32_t>>
    to_valid_chars(const std::u32string &input);

    std::vector<int64_t> input_to_ids(const std::u32string &input);

    std::vector<int64_t> hint_to_ids(const std::vector<std::u32string> &hints);

    std::vector<std::u32string> target_to_diacritics(const std::vector<uint8_t> &target_ids);

    std::u32string annotate_text_with_diacritics(
            const std::u32string& input,
            const std::vector<std::u32string>& diacritics,
            const std::unordered_set<char32_t>& removed_chars);

    std::u32string annotate_text_with_diacritics_taskeen(
            const std::u32string& input,
            const std::vector<std::u32string>& diacritics,
            const std::unordered_set<char32_t>& removed_chars,
            const std::vector<float>& logits,
            float taskeen_threshold);

public:

    void init(const char *moduleName, JNIEnv *pEnv);

    std::string tashkeelRun(const char *string);

    void release();

    void throwException(const char *string);
};


#endif //SCOUTIVI_APP_TTS_TASHKEEL_API_H
