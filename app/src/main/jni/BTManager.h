/*
 * BTManager.h
 *
 * Author: Tomás Tormo Franco
 */

#ifndef BTManager_H_
#define BTManager_H_

JNIEXPORT jobject JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_discoverDevices
        (JNIEnv *, jclass);

JNIEXPORT jobject JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_connect
        (JNIEnv *, jclass, jobject);

JNIEXPORT jobject JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_disconnect
        (JNIEnv *, jclass, jobjectArray);

JNIEXPORT jboolean JNICALL Java_net_kaisoz_droidstorm_bluetooth_BTManager_isConnected
        (JNIEnv *, jclass);

#endif /* BTManager_H_ */
