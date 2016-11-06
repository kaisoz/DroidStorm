/*
 * Connection.h
 *
 * Author: Tomás Tormo Franco
 *
 */

#ifndef CONNECTION_H_
#define CONNECTION_H_

JNIEXPORT jcharArray JNICALL Java_net_kaisoz_droidstorm_bluetooth_Connection_sendSingleCommand
        (JNIEnv *, jclass, jstring, jobject, jboolean);

JNIEXPORT jcharArray JNICALL Java_net_kaisoz_droidstorm_bluetooth_Connection_broadcastCommand
        (JNIEnv *, jclass, jobject, jboolean);

JNIEXPORT jcharArray JNICALL Java_net_kaisoz_droidstorm_bluetooth_Connection_waitForMessage
        (JNIEnv *, jclass, jstring);

#endif /* CONNECTION_H_ */
