/*
 * Connection.c
 *
 *	Contains functions to translates java messages to native messages and the other
 *	way round. Also, sends and retrieves messages to/from the robots and listens to follower
 *	messages.
 *
 *  Author: Tomás Tormo Franco
 */

#include <jni.h>
#include <time.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>

#include "AndroidLogging.h"
#include "DevicesManager.h"
#include "Connection.h"
#include "Exception.h"

// Message translation return values
#define OK 0
#define ERROR_SENDING_COMMAND -1
#define ERROR_RETRIEVING_MESSAGE -2
#define ERROR_WHOLE_MSG_NOT_RETRIEVED -3
#define ERROR_TRANSLATING_RESPONSE -4

/**
 * Translates a java command to a C command. The java char array is translated to a unsigned char array
 */
int translateToNative(JNIEnv *env, jobject *values, unsigned char **commandOut, int *cmLenOut) {

    unsigned char *wBuf = NULL;
    unsigned char *command = NULL;
    int cmdLen = 0;
    int i = 0;
    int j = 0;
    jchar value;

    //Get the Array size
    jint lenArgArray = (*env)->GetArrayLength(env, values);
    if ((*env)->ExceptionCheck(env) || lenArgArray <= 0) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return -1;
    }

    cmdLen = lenArgArray * 2;
    //Reserve memory for the command array
    command = (unsigned char *) malloc(sizeof(unsigned char) * (cmdLen));

    for (i = 0; i < lenArgArray; i++) {
        (*env)->GetCharArrayRegion(env, values, i, 1, &value);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            return -1;
        }

        //Mask the char in order to get the least significant byte and put it in the next field
        command[j + 1] = (unsigned char) (value & 0xFF);
        //Right sift the value in order to get the most significant byte
        value = value >> 8;
        //Mask the char in order to get the most significant byte
        command[j] = (unsigned char) (value & 0xFF);
        j = j + 2;
    }

    *cmLenOut = cmdLen;
    *commandOut = command;
    return 0;
}

/**
 * Translates a C command to a java command. The unsigned char array is translated to a java char array
 */
int translateToJava(JNIEnv *env, unsigned char *response, int respLen, jcharArray *jResponse) {

    jchar value;
    jcharArray translation;
    int i = 0;
    int j = 0;
    jsize jArraySize = 0;

    if (respLen % 2 != 0) {
        jArraySize = (respLen / 2) + 1;
    } else {
        jArraySize = (respLen / 2);
    }

    translation = (*env)->NewCharArray(env, jArraySize);
    if ((*env)->ExceptionCheck(env)) {
        return ERROR_TRANSLATING_RESPONSE;
    }

    for (i = 0; i < respLen; i = i + 2) {
        value = response[i] & 0xFF;
        value = value << 8;
        if (i != respLen - 1)
            value = value | (response[i + 1] & 0xFF);

        (*env)->SetCharArrayRegion(env, translation, j, 1, &value);
        if ((*env)->ExceptionCheck(env)) {
            return ERROR_TRANSLATING_RESPONSE;
        }
        j++;
    }

    *jResponse = translation;

    return OK;
}

/**
 * Waits for a follower message. The message will be waited at the jBtAddress
 */
JNIEXPORT jcharArray JNICALL Java_net_kaisoz_droidstorm_bluetooth_Connection_waitForMessage
        (JNIEnv *env, jclass obj, jstring jBtAddr) {

    struct timeval timeout = {0, 2};
    int rtn;
    int socket;
    unsigned char rBuf[1024];
    ssize_t readB;
    unsigned short int tamRet = 0;
    const char *btAddr;

    // Socket descriptors. Since we are just interested in one socket, it doesn't have to be initialized
    fd_set set;
    jboolean iscopy;
    jcharArray jResponse = NULL;

    btAddr = (*env)->GetStringUTFChars(env, jBtAddr, &iscopy);
    if (btAddr == NULL || (*env)->ExceptionCheck(env)) {
        throwBluetoothException(env, "Error retrieving bt address");
    }

    // Get the socket associated to the given Bluetooth address
    if (getSocketByBTAddr(&socket, btAddr) == -1) {
        throwBluetoothException(env, "Error retrieving socket");
    }

    FD_ZERO(&set);
    FD_SET(socket, &set);

    // Wait during 2 ms. If nothing received, return
    rtn = select(socket + 1, &set, NULL, NULL, &timeout);
    if (rtn < 0)
        return NULL;

    if (FD_ISSET(socket, &set)) {

        // Read first two bytes which will be the message size
        while ((readB = read(socket, &rBuf, 2)) == -1) { }

        if (readB != 2) {
            LOGE("Error retrieving response message from %d: %s", socket, strerror(errno));
            throwBluetoothException(env, "Error retrieving message");
        } else {
            // Get message size
            memcpy(&tamRet, &rBuf[0], 2);
            int rlength = (int) tamRet;

            // Read the rest of the message
            while ((readB = read(socket, &rBuf, rlength)) == -1) { }
            LOGD("Read %d bytes", readB);

            // We couln't receive the whole message. Throw an error
            if (readB != rlength) {
                LOGE("Couldn't recieve the whole message from %d. Got mismatch of %d and %d\n",
                     socket, readB, rlength);
                throwBluetoothException(env, "Error retrieving message");
            }

            LOGD("Return buffer size: %d", rlength);
            translateToJava(env, rBuf, rlength, &jResponse);
        }
    }

    // Return robot response
    return jResponse;
}


/*
 * Sends a command to the robot
 */
int doCommand(int socket, int waitForResponse, unsigned char *command, int cmdLen,
              unsigned char **response, int *respLen) {

    unsigned char rBuf[1024];
    unsigned char *wBuf = NULL;
    struct timeval timeout = {1, 2};
    ssize_t writtenB, readB;
    fd_set set;
    unsigned short int tamRet = 0;
    // Bluetooth headers size
    unsigned short int si = 0;
    int rtn = 0;
    int i = 0;

    wBuf = (unsigned char *) malloc((cmdLen + 2) * sizeof(char));
    si = (short int) cmdLen;

    memcpy(&wBuf[0], &si, 2);
    memcpy(&wBuf[2], command, cmdLen);

    writtenB = write(socket, wBuf, cmdLen + 2);
    if (writtenB == -1) {
        LOGE("Error sending command to: %d. Error: %s", socket, strerror(errno));
        return ERROR_SENDING_COMMAND;
    }

    if (waitForResponse) {
        FD_ZERO(&set);
        FD_SET(socket, &set);

        // If a response is requested, wait for it
        rtn = select(socket + 1, &set, NULL, NULL, &timeout);
        if (rtn > 0) {
            while ((readB = read(socket, &rBuf, 2)) == -1) { }

            if (readB != 2) {
                LOGE("Error retrieving response message from %d: %s", socket, strerror(errno));
                return ERROR_RETRIEVING_MESSAGE;
            } else {
                memcpy(&tamRet, &rBuf[0], 2);
                int rlength = (int) tamRet;
                while ((readB = read(socket, &rBuf, rlength)) == -1) { }
                LOGD("Read %d bytes", readB);

                if (readB != rlength) {
                    LOGE("Couldn't recieve the whole message from %d. Got mismatch of %d and %d\n",
                         socket, readB, rlength);
                    return ERROR_WHOLE_MSG_NOT_RETRIEVED;
                }

                if (response != NULL && respLen != NULL) {
                    *response = rBuf;
                    *respLen = rlength;
                }
            }
        } else {
            LOGE("No response recieved");
        }
    }

    return OK;
}

/**
 * Sends a command to all connected robots
 */
JNIEXPORT jcharArray JNICALL Java_net_kaisoz_droidstorm_bluetooth_Connection_broadcastCommand
        (JNIEnv *env, jclass obj, jobject data, jboolean response) {

    int connectedDevices = 0;
    int *sockets = NULL;
    unsigned char *command = NULL;
    unsigned char *responseData = NULL;
    int respLen = 0;
    int cmLen = 0;
    int rtn = 0;
    int i = 0;
    jcharArray jResponse = NULL;

    LOGI("Broadcast command");
    // Translate the command to native C
    translateToNative(env, data, &command, &cmLen);

    // Get sockets from all connected devices
    if (getSocketFromConnDevices(&sockets, &connectedDevices) == -1) {
        throwBluetoothException(env, "Error retrieving sockets");
    }

    if (response == JNI_TRUE && connectedDevices != 1) {
        response = JNI_FALSE;
    }

    // Send the command
    for (i = 0; i < connectedDevices; i++) {
        rtn = doCommand(sockets[i], response, command, cmLen, &responseData, &respLen);
        if (rtn != 0) {
            switch (rtn) {
                case ERROR_SENDING_COMMAND:
                    throwBluetoothException(env, "Error sending command to device");
                    break;
                case ERROR_RETRIEVING_MESSAGE:
                    throwBluetoothException(env, "Error retrieving response message from device");
                    break;
                case ERROR_WHOLE_MSG_NOT_RETRIEVED:
                    throwBluetoothException(env, "Couldn't recieve the whole message");
                    break;
            }
        }
    }

    if (response == JNI_TRUE) {
        // If response requested, translate it to java
        translateToJava(env, responseData, respLen, &jResponse);
    }
    return jResponse;
}

/**
 * Sends a command to a single robot
 */
JNIEXPORT jcharArray JNICALL Java_net_kaisoz_droidstorm_bluetooth_Connection_sendSingleCommand
        (JNIEnv *env, jclass obj, jstring jbtAddr, jobject data, jboolean response) {

    LOGI("sendSingleCommand");
    int connectedDevices = 0;
    int socket;
    const char *btAddr;
    jboolean iscopy;
    jcharArray jResponse = NULL;
    unsigned char *command = NULL;
    unsigned char *responseData = NULL;
    int cmLen = 0;
    int respLen = 0;
    int rtn = 0;
    int i = 0;

    btAddr = (*env)->GetStringUTFChars(env, jbtAddr, &iscopy);
    if (btAddr == NULL || (*env)->ExceptionCheck(env)) {
        throwBluetoothException(env, "Error retrieving bt address");
    }


    // Translate the command to native
    translateToNative(env, data, &command, &cmLen);

    // Get the socket associated to the given address
    if (getSocketByBTAddr(&socket, btAddr) == -1) {
        throwBluetoothException(env, "Error retrieving socket");
    }

    rtn = doCommand(socket, response, command, cmLen, &responseData, &respLen);
    if (rtn != 0) {
        switch (rtn) {
            case ERROR_SENDING_COMMAND:
                throwBluetoothException(env, "Error sending command to device");
                break;
            case ERROR_RETRIEVING_MESSAGE:
                throwBluetoothException(env, "Error retrieving response message from device");
                break;
            case ERROR_WHOLE_MSG_NOT_RETRIEVED:
                throwBluetoothException(env, "Couldn't recieve the whole message");
                break;
        }
    }

    if (response == JNI_TRUE) {
        // If response requested, translate it to java
        translateToJava(env, responseData, respLen, &jResponse);
    }

    return jResponse;
}
