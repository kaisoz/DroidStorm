#include <jni.h>
#include <stddef.h>
#include "AndroidLogging.h"

/**
 * Throws a Bluetooth exception in the Java layer
 * Author: Tomás Tormo Franco
 */

void throwBluetoothException(JNIEnv *env, char *msg) {
    jclass btException;
    btException = (*env)->FindClass(env,
                                    "net/kaisoz/droidstorm/bluetooth/exception/BluetoothException");
    if (btException == NULL) {
        LOGE("BTException is null");
        /* Unable to find the exception class, give up. */
        return;
    }
    (*env)->ThrowNew(env, btException, msg);
}
