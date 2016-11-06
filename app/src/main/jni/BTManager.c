/*
 * BTManager.c
 *
 *  Manages device discovery, device connection and disconnection. It includes the
 *	Bluez headers in order to be able to interact with the Bluetooth radio.
 *
 *	Author: Tomás Tormo Franco
 */
#include <jni.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/rfcomm.h>
#include <sys/socket.h>

#include "AndroidLogging.h"
#include "DevicesManager.h"
#include "BTManager.h"
#include "Exception.h"

/**
 * Searches for devices
 */
JNIEXPORT jobject JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_discoverDevices
        (JNIEnv *env, jclass obj) {
    inquiry_info *ii = NULL;
    int num_rsp;
    int max_rsp = 255;
    int dev_id, sock, flags;
    int len = 8;
    int i;
    char addr[19] = {0};
    char name[248] = {0};
    char exMsg[50];
    char *legoVendorID = "00:16:53";

    jclass jIndexedMapClass;
    jobject jIndexedMapObject;
    jmethodID jputMethod;
    jmethodID jconstructor;
    jstring jname;
    jstring jmac;

    LOGI("Discovering devices...");
    jIndexedMapClass = (*env)->FindClass(env, "net/kaisoz/droidstorm/util/IndexedMap");
    if (jIndexedMapClass == NULL) {
        /* if jIndexedMapClass is NULL, an exception has already been thrown */
        return NULL;
    }

    jconstructor = (*env)->GetMethodID(env, jIndexedMapClass, "<init>", "()V");
    if (jconstructor == NULL) {
        /* if jconstructor is NULL, an exception has already been thrown */
        return NULL;
    }

    jputMethod = (*env)->GetMethodID(env, jIndexedMapClass, "put",
                                     "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (jputMethod == NULL) {
        /* if jputMethod is NULL, an exception has already been thrown */
        return NULL;
    }

    jIndexedMapObject = (*env)->NewObject(env, jIndexedMapClass, jconstructor);
    if (jIndexedMapClass == NULL) {
        /* if jIndexedMapClass is NULL, an exception has already been thrown */
        return NULL;
    }

    dev_id = hci_get_route(NULL);
    sock = hci_open_dev(dev_id);
    if (dev_id < 0 || sock < 0) {
        LOGE("Error opening local bluetooth device: %s", strerror(errno));
        throwBluetoothException(env, "Error opening local bluetooth device");
    }

    /* Set IREQ_CACHE_FLUSH flags to clean detected devices cache. Otherwise,
     * they would be included in the response even though they are not in range anymore*/
    flags = IREQ_CACHE_FLUSH;
    ii = (inquiry_info *) malloc(max_rsp * sizeof(inquiry_info));

    num_rsp = hci_inquiry(dev_id, len, max_rsp, NULL, &ii, flags);
    if (num_rsp < 0) {
        LOGE("Error inquiring device: %s", strerror(errno));
        throwBluetoothException(env, "Error inquiring device");
    }

    if (num_rsp > 0) {

        LOGI("Found %d devices", num_rsp);
        for (i = 0; i < num_rsp; i++) {

            if (ba2str(&(ii + i)->bdaddr, addr) < 0) {
                LOGE("Error getting bluetooth address");
                break;
            }

            LOGD("Found device with address: %s", addr);

            /* Filter by Minstorm devices.
             * Will only return devices whose address has the Lego Vendor Id in its first part
             */
            if (strncmp(legoVendorID, addr, 8) == 0) {
                memset(name, 0, sizeof(name));

                if (hci_read_remote_name(sock, &(ii + i)->bdaddr, sizeof(name), name, 0) < 0)
                    strcpy(name, "[unknown]");

                jname = (*env)->NewStringUTF(env, name);
                jmac = (*env)->NewStringUTF(env, addr);
                if (jname == NULL || jmac == NULL) {
                    /* if jname is NULL or jmac is NULL, an exception has already been thrown. Let's continue to the next found device */
                    break;
                }

                (*env)->CallObjectMethod(env, jIndexedMapObject, jputMethod, jname, jmac);
                // If there was an Exception calling the method, we will print it and continue to the next device
                if ((*env)->ExceptionCheck(env)) {
                    (*env)->ExceptionDescribe(env);
                    (*env)->ExceptionClear(env);
                }
            }
        }

        close(sock);
        free(ii);
    }

    return jIndexedMapObject;
}

/**
 * Connects to the devices whose bluetooth address has been passed
 */
JNIEXPORT jobject JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_connect
        (JNIEnv *env, jclass obj, jobjectArray jbtAddresses) {

    int rv;
    struct sockaddr_rc addr = {0};
    int flags;
    int status;
    char dest[18];
    int newsocket = 0;;
    int rtn = 0;

    jboolean iscopy;
    jstring jBdAddr;
    jstring jDeviceName;

    const char *deviceName;
    const char *btAddr;

    char *connected[7];
    jobjectArray jconnected;
    int connCounter = 0;
    char *error[7];
    jobjectArray jerror;
    int errorCounter = 0;
    int i = 0;

    //Get the Array size
    jint lenArgArray = (*env)->GetArrayLength(env, jbtAddresses);
    if ((*env)->ExceptionCheck(env) || lenArgArray <= 0) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return NULL;
    }

    for (i = 0; i < lenArgArray; i++) {

        jstring jBdAddr = (jstring) (*env)->GetObjectArrayElement(env, jbtAddresses, i);
        if (jBdAddr == NULL || (*env)->ExceptionCheck(env)) {
            LOGE("Error getting value from IndexedMap");
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            break;
        }

        btAddr = (*env)->GetStringUTFChars(env, jBdAddr, &iscopy);
        if (btAddr == NULL || (*env)->ExceptionCheck(env)) {
            LOGE("Error getting device name");
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            break;
        }

        LOGI("Connecting to %s (%s)...", deviceName, btAddr);

        // Open socket
        newsocket = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
        if (newsocket < 0) {
            LOGE("Error creating socket: %s", strerror(errno));
            error[errorCounter] = (char *) malloc(strlen(deviceName) * sizeof(char));
            strcpy(error[errorCounter], deviceName);
            errorCounter++;
            break;
        }

        // Set the connection parameters (who to connect to)
        addr.rc_family = AF_BLUETOOTH;
        addr.rc_channel = (uint8_t) 1;
        str2ba(btAddr, &addr.rc_bdaddr);

        // connect
        status = connect(newsocket, (struct sockaddr *) &addr, sizeof(addr));
        if (status == -1) {
            // Error connecting to the device. Add it to the "error" array
            LOGE("Error connecting to %s", btAddr);
            error[errorCounter] = (char *) malloc(strlen(btAddr) * sizeof(char));
            strcpy(error[errorCounter], btAddr);
            errorCounter++;

        } else {
            // Device conneted succesfully. Add it to the manager and to the "success" array
            rtn = addDevice(btAddr, newsocket, 1);
            connected[connCounter] = (char *) malloc(strlen(btAddr) * sizeof(char));
            strcpy(connected[connCounter], btAddr);
            connCounter++;
        }

        (*env)->ReleaseStringUTFChars(env, jBdAddr, btAddr);
    }

    // Make string array with device names of connected devices
    jerror = (*env)->NewObjectArray(env, errorCounter, (*env)->FindClass(env, "java/lang/String"),
                                    0);
    for (i = 0; i < errorCounter; i++) {
        jBdAddr = (*env)->NewStringUTF(env, error[i]);
        if (jBdAddr == NULL) return NULL;
        (*env)->SetObjectArrayElement(env, jerror, i, jBdAddr);
        if ((*env)->ExceptionCheck(env)) {
            return NULL;
        }
    }

    // Make string array with device names of error connecting devices
    jconnected = (*env)->NewObjectArray(env, connCounter,
                                        (*env)->FindClass(env, "java/lang/String"), 0);
    for (i = 0; i < connCounter; i++) {
        jBdAddr = (*env)->NewStringUTF(env, connected[i]);
        (*env)->SetObjectArrayElement(env, jconnected, i, jBdAddr);
    }

    jclass jIndexedMapClass = (*env)->FindClass(env, "net/kaisoz/droidstorm/util/IndexedMap");
    if (jIndexedMapClass == NULL) {
        /* if jIndexedMapClass is NULL, an exception has already been thrown */
        return NULL;
    }

    jmethodID jconstructor = (*env)->GetMethodID(env, jIndexedMapClass, "<init>", "()V");
    if (jconstructor == NULL) {
        /* if jconstructor is NULL, an exception has already been thrown */
        return NULL;
    }

    jmethodID jputMethod = (*env)->GetMethodID(env, jIndexedMapClass, "put",
                                               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (jputMethod == NULL) {
        /* if jputMethod is NULL, an exception has already been thrown */
        return NULL;
    }

    jobject jIndexedMapObject = (*env)->NewObject(env, jIndexedMapClass, jconstructor);
    if (jIndexedMapClass == NULL) {
        /* if jIndexedMapClass is NULL, an exception has already been thrown */
        return NULL;
    }

    // Set values and return
    (*env)->CallObjectMethod(env, jIndexedMapObject, jputMethod,
                             (*env)->NewStringUTF(env, "success"), jconnected);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }

    (*env)->CallObjectMethod(env, jIndexedMapObject, jputMethod, (*env)->NewStringUTF(env, "error"),
                             jerror);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }

    return jIndexedMapObject;
}

/**
 * Disconnect from the devices whose address has been passed
 */
JNIEXPORT jobject JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_disconnect
        (JNIEnv *env, jclass obj, jobjectArray jbtAddresses) {
    int i = 0;
    int rtn;
    int socket = 0;
    jstring jbdAddr;
    jboolean isCopy = JNI_FALSE;

    int copy = 1;
    const char *btAddr;

    char *disconnectedAddr[7];
    jobjectArray jDisconnectedAddr = NULL;
    int discCounter = 0;
    char *error[7];
    jobjectArray jerror = NULL;
    int errorCounter = 0;

    LOGI("Disconnection...");
    jsize length = (*env)->GetArrayLength(env, jbtAddresses);

    jclass jIndexedMapClass = (*env)->FindClass(env, "net/kaisoz/droidstorm/util/IndexedMap");
    if (jIndexedMapClass == NULL) {
        /* if jIndexedMapClass is NULL, an exception has already been thrown */
        return NULL;
    }

    jmethodID jconstructor = (*env)->GetMethodID(env, jIndexedMapClass, "<init>", "()V");
    if (jconstructor == NULL) {
        /* if jconstructor is NULL, an exception has already been thrown */
        return NULL;
    }

    jmethodID jputMethod = (*env)->GetMethodID(env, jIndexedMapClass, "put",
                                               "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (jputMethod == NULL) {
        /* if jputMethod is NULL, an exception has already been thrown */
        return NULL;
    }

    jobject jIndexedMapObject = (*env)->NewObject(env, jIndexedMapClass, jconstructor);
    if (jIndexedMapObject == NULL) {
        /* if jIndexedMapClass is NULL, an exception has already been thrown */
        return NULL;
    }


    for (i = 0; i < length; i++) {
        copy = 1;
        jbdAddr = (jstring) (*env)->GetObjectArrayElement(env, jbtAddresses, i);
        if ((*env)->ExceptionCheck(env)) {
            return NULL;
        }
        btAddr = (*env)->GetStringUTFChars(env, jbdAddr, &isCopy);
        if ((*env)->ExceptionCheck(env)) {
            return NULL;
        }
        LOGD("addr de %d: %s", i, btAddr);
        rtn = getSocketByBTAddr(&socket, btAddr);
        LOGD("RFCOMM disconnect, handle %d", socket);
        // Closing channel, further sends and receives will be disallowed.
        if (shutdown(socket, SHUT_RDWR) < 0) {
            LOGE("shutdown failed. [%d] %s", errno, strerror(errno));
            copy = 0;
        }
        if (close(socket) < 0) {
            LOGE("Failed to close socket. [%d] %s", errno, strerror(errno));
            copy = 0;
        }
        if (copy == 1) {
            // Disconnected successfully. Add it to the "success" array
            disconnectedAddr[i] = (char *) malloc(strlen(btAddr) * sizeof(char));
            strcpy(disconnectedAddr[i], btAddr);
            delDevice(btAddr);
            discCounter++;
        } else {
            // Error disconnecting. Add it to the "error" array
            error[i] = (char *) malloc(strlen(btAddr) * sizeof(char));
            strcpy(error[i], btAddr);
            delDevice(btAddr);
            errorCounter++;
        }
    }

    jDisconnectedAddr = (*env)->NewObjectArray(env, discCounter,
                                               (*env)->FindClass(env, "java/lang/String"), 0);
    for (i = 0; i < discCounter; i++) {
        jbdAddr = (*env)->NewStringUTF(env, disconnectedAddr[i]);
        if (jbdAddr == NULL) return NULL;
        (*env)->SetObjectArrayElement(env, jDisconnectedAddr, i, jbdAddr);
        if ((*env)->ExceptionCheck(env)) {
            return NULL;
        }
    }

    jerror = (*env)->NewObjectArray(env, errorCounter, (*env)->FindClass(env, "java/lang/String"),
                                    0);
    for (i = 0; i < errorCounter; i++) {
        jbdAddr = (*env)->NewStringUTF(env, error[i]);
        if (jbdAddr == NULL) return NULL;
        (*env)->SetObjectArrayElement(env, jerror, i, jbdAddr);
        if ((*env)->ExceptionCheck(env)) {
            return NULL;
        }
    }

    //Set values and return
    (*env)->CallObjectMethod(env, jIndexedMapObject, jputMethod,
                             (*env)->NewStringUTF(env, "success"), jDisconnectedAddr);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }

    (*env)->CallObjectMethod(env, jIndexedMapObject, jputMethod, (*env)->NewStringUTF(env, "error"),
                             jerror);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }

    return jIndexedMapObject;
}

/**
 * Returns true if the phone is connected to any device
 */
JNIEXPORT jboolean JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_isConnected
        (JNIEnv *env, jclass obj) {
    int connected = isConnected();
    return connected;
}
