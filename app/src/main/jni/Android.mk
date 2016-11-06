# Gets the path for local directory
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libbluetooth
LOCAL_SRC_FILES := $(LOCAL_PATH)/libs/libbluetooth.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := BTCommunication
LOCAL_LDLIBS := -llog
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SRC_FILES := BTManager.c Connection.c DevicesManager.c Exception.c
LOCAL_SHARED_LIBRARIES := libbluetooth
include $(BUILD_SHARED_LIBRARY)

