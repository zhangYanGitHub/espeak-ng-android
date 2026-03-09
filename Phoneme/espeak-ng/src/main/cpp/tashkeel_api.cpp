//
// Created by Zhang, Yan (Yan) on 2024/7/30.
//

#include <jni.h>
#include "tashkeel_api.h"

// 初始化静态映射
const std::unordered_map<std::u32string, int64_t> TashkeelApi::HINT_ID_MAP = {
        {U"_",  0},
        {U"",   1},
        {U"ً",  2},
        {U"ٌ",  3},
        {U"ٍ",  4},
        {U"َ",  5},
        {U"ُ",  6},
        {U"ِ",  7},
        {U"ًّ", 8},
        {U"ٌّ", 9},
        {U"ٍّ", 10},
        {U"َّ", 11},
        {U"ُّ", 12},
        {U"ِّ", 13},
        {U"ْ",  14},
        {U"ّ",  15},
};

const std::unordered_map<char32_t, int64_t> TashkeelApi::INPUT_ID_MAP = {
        {U'\u005F', 0}, // "_"
        {U'\u0020', 1}, // " "
        {U'\u0023', 2}, // "#"
        {U'\u0021', 3}, // "!"
        {U'\u0022', 4}, // "\""
        {U'\u0028', 5}, // "("
        {U'\u0029', 6}, // ")"
        {U'\u002D', 7}, // "-"
        {U'\u002E', 8}, // "."
        {U'\u002F', 9}, // "/"
        {U'\u003A', 10}, // ":"
        {U'\u005B', 11}, // "["
        {U'\u005D', 12}, // "]"
        {U'\u00AB', 13}, // "«"
        {U'\u00BB', 14}, // "»"
        {U'\u060C', 15}, // "،"
        {U'\u061B', 16}, // "؛"
        {U'\u061F', 17}, // "؟"
        {U'\u0621', 18}, // "ء"
        {U'\u0622', 19}, // "آ"
        {U'\u0623', 20}, // "أ"
        {U'\u0624', 21}, // "ؤ"
        {U'\u0625', 22}, // "إ"
        {U'\u0626', 23}, // "ئ"
        {U'\u0627', 24}, // "ا"
        {U'\u0628', 25}, // "ب"
        {U'\u0629', 26}, // "ة"
        {U'\u062A', 27}, // "ت"
        {U'\u062B', 28}, // "ث"
        {U'\u062C', 29}, // "ج"
        {U'\u062D', 30}, // "ح"
        {U'\u062E', 31}, // "خ"
        {U'\u062F', 32}, // "د"
        {U'\u0630', 33}, // "ذ"
        {U'\u0631', 34}, // "ر"
        {U'\u0632', 35}, // "ز"
        {U'\u0633', 36}, // "س"
        {U'\u0634', 37}, // "ش"
        {U'\u0635', 38}, // "ص"
        {U'\u0636', 39}, // "ض"
        {U'\u0637', 40}, // "ط"
        {U'\u0638', 41}, // "ظ"
        {U'\u0639', 42}, // "ع"
        {U'\u063A', 43}, // "غ"
        {U'\u0641', 44}, // "ف"
        {U'\u0642', 45}, // "ق"
        {U'\u0643', 46}, // "ك"
        {U'\u0644', 47}, // "ل"
        {U'\u0645', 48}, // "م"
        {U'\u0646', 49}, // "ن"
        {U'\u0647', 50}, // "ه"
        {U'\u0648', 51}, // "و"
        {U'\u0649', 52}, // "ى"
        {U'\u064A', 53}  // "ي"
};
const std::unordered_map<uint8_t, std::u32string> TashkeelApi::TARGET_ID_MAP = {
        {0,  U"_"},
        {1,  U""},
        {2,  U"ً"},
        {3,  U"ٌ"},
        {4,  U"ٍ"},
        {5,  U"َ"},
        {6,  U"ُ"},
        {7,  U"ِ"},
        {8,  U"ًّ"},
        {9,  U"ٌّ"},
        {10, U"ٍّ"},
        {11, U"َّ"},
        {12, U"ُّ"},
        {13, U"ِّ"},
        {14, U"ْ"}
};
const std::unordered_set<char32_t> TashkeelApi::ARABIC_DIACRITICS = {
        U'\u0640', // Arabic Tatweel
        U'\u064B', // Arabic Fatha
        U'\u064C', // Arabic Damma
        U'\u064D', // Arabic Kasra
        U'\u064E', // Arabic Fatha
        U'\u064F', // Arabic Damma
        U'\u0650', // Arabic Kasra
        U'\u0651', // Arabic Shadda
        U'\u0652', // Arabic Sukun
        U'\u061F', // Arabic Question Mark
        U'\u060C', // Arabic Comma
        U'\u061B'  // Arabic Semicolon
};
const std::unordered_map<std::u32string, std::u32string> TashkeelApi::NORMALIZED_DIAC_MAP = {
        {U"َّ", U"َّ"}, // Arabic Fatha
        {U"ًّ", U"ًّ"}, // Arabic Tanwin Fatha
        {U"ُّ", U"ُّ"}, // Arabic Damma
        {U"ٌّ", U"ٌّ"}, // Arabic Tanwin Damma
        {U"ِّ", U"ِّ"}, // Arabic Kasra
        {U"ٍّ", U"ٍّ"}  // Arabic Tanwin Kasra
};
// 使用类外静态初始化
const std::unordered_set<uint8_t> TashkeelApi::TARGET_META_CHAR_IDS = [] {
    std::unordered_set<uint8_t> ids;
    auto it = INPUT_ID_MAP.find('_');
    if (it != INPUT_ID_MAP.end()) {
        ids.insert(static_cast<uint8_t>(it->second));
    }
    return ids;
}();

bool TashkeelApi::is_diacritic_char(char32_t c) const {
    return ARABIC_DIACRITICS.find(c) != ARABIC_DIACRITICS.end();
}

std::pair<std::u32string, std::vector<std::u32string>> TashkeelApi::extract_chars_and_diacritics(
        const std::u32string &input_text,
        bool normalize_diacritics) const {
    std::u32string clean_chars;
    std::vector<std::u32string> diacritics;
    std::u32string pending_diac;

    // Remove leading diacritics
    auto it = input_text.begin();
    while (it != input_text.end() && is_diacritic_char(*it)) {
        ++it;
    }

    for (; it != input_text.end(); ++it) {
        char32_t c = *it;
        if (is_diacritic_char(c)) {
            pending_diac += c;
        } else {
            clean_chars += c;
            diacritics.push_back(pending_diac);
            pending_diac.clear();
        }
    }
    // Remove the last character and first empty diacritic
    if (!clean_chars.empty()) {
        clean_chars.pop_back();
    }
    if (!diacritics.empty() && diacritics.front().empty()) {
        diacritics.erase(diacritics.begin());
    }

    if (normalize_diacritics) {
        for (auto &diac: diacritics) {
            if (HINT_ID_MAP.find(diac) == HINT_ID_MAP.end()) {
                auto it = NORMALIZED_DIAC_MAP.find(diac);
                if (it != NORMALIZED_DIAC_MAP.end()) {
                    diac = it->second;
                } else {
                    diac.clear();
                }
            }
        }
    }

    return {clean_chars, diacritics};
}

std::pair<std::u32string, std::unordered_set<char32_t>> TashkeelApi::to_valid_chars(
        const std::u32string &input) const {
    std::u32string valid;
    std::unordered_set<char32_t> invalid;

    for (char32_t c: input) {
        if (INPUT_ID_MAP.find(c) != INPUT_ID_MAP.end() || is_diacritic_char(c)) {
            valid += c;
        } else if (std::find(NUMERALS.begin(), NUMERALS.end(), c) != NUMERALS.end()) {
            valid += NUMERAL_SYMBOL;
        } else {
            invalid.insert(c);
        }
    }

    return {valid, invalid};
}

// 验证字符并分类
std::pair<std::u32string, std::unordered_set<char32_t>>
TashkeelApi::to_valid_chars(const std::u32string &input) {
    std::u32string valid;
    std::unordered_set<char32_t> invalid;

    for (char32_t c: input) {
        if (INPUT_ID_MAP.find(c) != INPUT_ID_MAP.end() || is_diacritic_char(c)) {
            valid += c;
        } else if (std::find(NUMERALS.begin(), NUMERALS.end(), c) != NUMERALS.end()) {
            valid += NUMERAL_SYMBOL;
        } else {
            invalid.insert(c);
        }
    }

    return {valid, invalid};
}

// 输入字符转换为 ID
std::vector<int64_t> TashkeelApi::input_to_ids(const std::u32string &input) {
    std::vector<int64_t> ids;
    for (char32_t c: input) {
        auto it = INPUT_ID_MAP.find(c);
        if (it != INPUT_ID_MAP.end()) {
            ids.push_back(it->second);
        }
    }
    return ids;
}

// 提示字符串转换为 ID
std::vector<int64_t> TashkeelApi::hint_to_ids(const std::vector<std::u32string> &hints) {
    std::vector<int64_t> ids;
    for (const auto &hint: hints) {
        auto it = HINT_ID_MAP.find(hint);
        if (it != HINT_ID_MAP.end()) {
            ids.push_back(it->second);
        }
    }
    return ids;
}

// 目标 ID 转换为音标
std::vector<std::u32string>
TashkeelApi::target_to_diacritics(const std::vector<uint8_t> &target_ids) {
    std::vector<std::u32string> diacritics;
    for (uint8_t id: target_ids) {
        if (TARGET_META_CHAR_IDS.find(id) == TARGET_META_CHAR_IDS.end()) {
            auto it = TARGET_ID_MAP.find(id);
            if (it != TARGET_ID_MAP.end()) {
                diacritics.push_back(it->second);
            }
        }
    }
    return diacritics;
}

// 将音标插入到文本中
std::u32string TashkeelApi::annotate_text_with_diacritics(
        const std::u32string &input,
        const std::vector<std::u32string> &diacritics,
        const std::unordered_set<char32_t> &removed_chars) {
    std::u32string output;
    auto diac_iter = diacritics.begin();

    for (char32_t c: input) {
        if (ARABIC_DIACRITICS.find(c) != ARABIC_DIACRITICS.end()) {
            continue;
        } else if (removed_chars.find(c) != removed_chars.end()) {
            output += c;
        } else {
            output += c;
            if (diac_iter != diacritics.end()) {
                output += *diac_iter;
                ++diac_iter;
            }
        }
    }
    return output;
}

// 将音标和概率值插入到文本中
std::u32string TashkeelApi::annotate_text_with_diacritics_taskeen(
        const std::u32string &input,
        const std::vector<std::u32string> &diacritics,
        const std::unordered_set<char32_t> &removed_chars,
        const std::vector<float> &logits,
        float taskeen_threshold) {
    const char32_t sukoon = U'\u0627'; // 假设 'sukoon' 的 Unicode 编码是 U+0630
    std::u32string output;
    auto diac_iter = diacritics.begin();
    auto logit_iter = logits.begin();

    for (char32_t c: input) {
        if (ARABIC_DIACRITICS.find(c) != ARABIC_DIACRITICS.end()) {
            continue;
        } else if (removed_chars.find(c) != removed_chars.end()) {
            output += c;
        } else {
            output += c;
            if (diac_iter != diacritics.end() && logit_iter != logits.end()) {
                if (*logit_iter > taskeen_threshold) {
                    output += sukoon;
                } else {
                    output += *diac_iter;
                }
                ++diac_iter;
                ++logit_iter;
            }
        }
    }
    return output;
}

void TashkeelApi::init(const char *moduleName, JNIEnv *pEnv) {
    m_env = pEnv;
    m_state.env = Ort::Env(OrtLoggingLevel::ORT_LOGGING_LEVEL_WARNING,
                           "new-tasheel");
    m_state.env.DisableTelemetryEvents();
    m_state.options.SetInterOpNumThreads(2);
    m_state.options.SetGraphOptimizationLevel(GraphOptimizationLevel::ORT_ENABLE_ALL);
    m_state.options.SetExecutionMode(ExecutionMode::ORT_PARALLEL);
    m_state.onnx = Ort::Session(m_state.env, moduleName, m_state.options);
    LOGV("init successfully native");
}


std::string TashkeelApi::tashkeelRun(const char *string) {
    // 将 const char* 转换为 std::string
    std::string input_str(string);
    std::wstring_convert<std::codecvt_utf8<char32_t>, char32_t> converter;

    auto input_utf32 = converter.from_bytes(input_str);

    if (input_utf32.size() > CHAR_LIMIT) {
        std::string errorMessage =
                "Input too long must < " + std::to_string(CHAR_LIMIT) + ": " + string;
        throwException(errorMessage.c_str());

    }
    auto [input_text, removed_chars] = to_valid_chars(input_utf32);
    auto [input_text_cleaned, diacritics] = extract_chars_and_diacritics(input_text, true);

    auto input_ids = input_to_ids(input_text_cleaned);
    auto diac_ids = hint_to_ids(diacritics);
    size_t seq_length = input_ids.size();
    auto allocator = m_state.allocator;
    auto memoryInfo = Ort::MemoryInfo::CreateCpu(
            OrtAllocatorType::OrtArenaAllocator, OrtMemType::OrtMemTypeDefault);

    // Create tensors
    std::vector<int64_t> shape = {1, static_cast<int64_t>(seq_length)};
    auto input_ids_tensor = Ort::Value::CreateTensor(
            memoryInfo, input_ids.data(), input_ids.size(), shape.data(),
            shape.size());

    auto diac_ids_tensor = Ort::Value::CreateTensor(
            memoryInfo, diac_ids.data(), diac_ids.size(), shape.data(),
            shape.size());

    // Define input lengths
    std::vector<int64_t> input_length = {static_cast<int64_t>(seq_length)};
    std::vector<int64_t> length_shape = {1};
    auto input_length_tensor = Ort::Value::CreateTensor(
            memoryInfo, input_length.data(), input_length.size(), length_shape.data(),
            length_shape.size());

    // Prepare input names and output names
    std::vector<const char *> input_names = {"char_inputs", "diac_inputs", "input_lengths"};
    std::vector<const char *> output_names = {"predictions", "logits"};
    std::vector<Ort::Value> inputTensors;
    inputTensors.reserve(3);
    inputTensors.push_back(std::move(input_ids_tensor));
    inputTensors.push_back(std::move(diac_ids_tensor));
    inputTensors.push_back(std::move(input_length_tensor));


    std::vector<Ort::Value> output_tensors;

    try {
        output_tensors = m_state.onnx.Run(Ort::RunOptions{nullptr}, input_names.data(),
                                          inputTensors.data(), inputTensors.size(),
                                          output_names.data(), output_names.size());
    } catch (const Ort::Exception &e) {
        std::string errorMessage =
                "tashkeelRun Ort error " + std::string(e.what());
        throwException(errorMessage.c_str());
    } catch (const std::exception &e) {
        LOGE("Exception: %s", e.what());
        std::string errorMessage =
                "tashkeelRun error " + std::string(e.what());
        throwException(errorMessage.c_str());
    }

    // Check the size of output tensors
    if (output_tensors.size() < 2) {
        std::string errorMessage =
                "Unexpected number of output tensors";
        throwException(errorMessage.c_str());
    }

    // Extract target_ids
    auto target_ids_tensor = std::move(output_tensors[0]);
    auto logits_tensor = std::move(output_tensors[1]);

    // Extract target_ids
    auto target_ids_info = target_ids_tensor.GetTensorTypeAndShapeInfo();
    size_t target_ids_size = target_ids_info.GetElementCount();
    std::vector<uint8_t> target_ids(target_ids_size);
    std::memcpy(target_ids.data(), target_ids_tensor.GetTensorData<uint8_t>(),
                target_ids_size * sizeof(uint8_t));

    // Extract logits
    auto logits_info = logits_tensor.GetTensorTypeAndShapeInfo();
    size_t logits_size = logits_info.GetElementCount();
    std::vector<float> logits(logits_size);
    std::memcpy(logits.data(), logits_tensor.GetTensorData<float>(), logits_size * sizeof(float));

    auto result_diacritics = target_to_diacritics(target_ids);
    std::u32string final_text;
    if (!taskeen_threshold.has_value()) {
        final_text = annotate_text_with_diacritics(input_utf32, result_diacritics, removed_chars);
    } else {
        final_text = annotate_text_with_diacritics_taskeen(input_utf32, result_diacritics,
                                                           removed_chars, logits,
                                                           taskeen_threshold.value());

    }
    auto final_text_ut8 = converter.to_bytes(final_text);

    LOGV("tashkeel %s", final_text_ut8.c_str());

    //clean up
    for (auto & inputTensor : inputTensors) {
        Ort::detail::OrtRelease(inputTensor.release());
    }
    for (auto & output_tensor : output_tensors) {
        Ort::detail::OrtRelease(output_tensor.release());
    }

    return final_text_ut8;
}

void TashkeelApi::throwException(const char *string) {
    jclass exceptionClass = m_env->FindClass(
            "com/telenav/scoutivi/tts/espeak/exception/TashkeelException");
    if(exceptionClass != nullptr){
        m_env->ThrowNew(exceptionClass, string);
    } else{
        LOGE("FindClass TashkeelException failed");
        throw std::runtime_error(string);
    }
}

void TashkeelApi::release() {
    // 释放 ONNX Runtime 会话
    m_state.onnx.release();
    m_state.env.release();
}
