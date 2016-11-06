#ifndef ANDROIDLOGGING_H_
#define ANDROIDLOGGING_H_

#include <android/log.h>

// Logging macros

#define TAG  "Communication",
#define __LOG(l, ...) __android_log_print(l, "COMM", __VA_ARGS__)

#define LOGD(...) __LOG(ANDROID_LOG_DEBUG, ##__VA_ARGS__)
#define LOGI(...) __LOG(ANDROID_LOG_INFO,  ##__VA_ARGS__)
#define LOGW(...) __LOG(ANDROID_LOG_WARN,  ##__VA_ARGS__)
#define LOGE(...) __LOG(ANDROID_LOG_ERROR, ##__VA_ARGS__)


#endif /* ANDROIDLOGGING_H_ */
