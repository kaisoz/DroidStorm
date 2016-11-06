#include "DevicesManager.h"
#include "AndroidLogging.h"

/**
 * Holds all connected devices
 * Author: Tomás Tormo Franco
 */

/**
 * Device descriptor.
 * Holds information of its address, its associated socket and its state
 */
typedef struct descriptor {
    char *btAddr;
    int socket;
    int connected;

    struct descriptor *next;
    struct descriptor *before;

} deviceDescriptor;

deviceDescriptor *head = NULL;
deviceDescriptor *last = NULL;
int numDevices = 0;
int connectedDevices = 0;

/**
 *  Returns 1 if any device connected
 */
int isConnected() {
    if (connectedDevices > 0) {
        return 1;
    } else {
        return 0;
    }
}

/**
 *  Adds a device to the list
 */
int addDevice(const char *btAddr, int socket, int connected) {
    deviceDescriptor *new;

    if (btAddr == NULL) {
        return -1;
    }

    if (connected > 0) {
        connected = CONNECTED;
        connectedDevices++;
    }

    new = (deviceDescriptor *) malloc(sizeof(deviceDescriptor));
    if (!new) {
        return -1;
    }

    new->btAddr = malloc(strlen(btAddr) + 1);
    strcpy(new->btAddr, btAddr);

    new->socket = socket;
    new->connected = connected;
    new->next = NULL;
    new->before = NULL;

    if (head == NULL) {
        head = new;
        last = new;
    } else {
        last->next = new;
        new->before = last;
        last = new;
    }

    numDevices++;
    return 0;
}

/**
 *  Removes a device from the list
 */
int delDevice(const char *btaddr) {
    if (numDevices == 0) {
        return -1;
    }

    deviceDescriptor *aux = head;
    int found = FALSE;
    int i = 1;

    while (i <= numDevices && found == FALSE) {
        if (strcmp(aux->btAddr, btaddr) == 0) {
            found = TRUE;
            LOGE("found = true");
        } else {
            aux = aux->next;
            i++;
        }
    }

    if (found == TRUE) {
        if (i == 1) {
            head = head->next;
            goto freeAux;
        } else if (i == numDevices) {
            last = last->before;
            goto freeAux;
        } else {
            aux->before->next = aux->next;
            goto freeAux;
        }
    } else {
        free(aux);
        aux = NULL;
        return -1;
    }

    freeAux:
    numDevices--;
    if (aux->connected == TRUE) {
        connectedDevices--;
    }
    free(aux);
    aux = NULL;
    return 0;
}

/**
 * Returns the sockets of all connected devices
 */
int getSocketFromConnDevices(int **sockets, int *length) {
    if (connectedDevices == 0) {
        return -1;
    }

    int *socketArray = (int *) malloc(connectedDevices * sizeof(int));
    int i = 0;

    deviceDescriptor *aux = head;
    while (aux != NULL && i < connectedDevices) {
        if (aux->connected == TRUE) {
            socketArray[i] = aux->socket;
        }
        aux = aux->next;
        i++;
    }

    *length = connectedDevices;
    *sockets = socketArray;
    aux = NULL;
    return 0;
}

/**
 * Returns the socket associated to the given Bluetooth address
 */
int getSocketByBTAddr(int *socket, const char *btAddr) {
    if (connectedDevices == 0) {
        return -1;
    }

    int i = 0;
    deviceDescriptor *aux = head;
    while (aux != NULL && i < connectedDevices) {
        if (aux->connected == TRUE && strcmp(aux->btAddr, btAddr) == 0) {
            *socket = aux->socket;
            break;
        }
        aux = aux->next;
        i++;
    }

    aux = NULL;
    if (i == connectedDevices)
        return -1;
    else
        return 0;
}


