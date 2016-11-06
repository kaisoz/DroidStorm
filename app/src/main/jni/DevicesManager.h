/*
* DevicesManager.h
*
* Author Tomás Tormo Franco
*/

#ifndef DEVICESMANAGER_H_
#define DEVICESMANAGER_H_

#define TRUE 1
#define FALSE 0

#define CONNECTED 1
#define NOT_CONNECTED 0

#include <string.h>

int addDevice(const char *btAddr, int socket, int connected);

int delDevice(const char *btAddr);

int getSocketFromConnDevices(int **sockets, int *length);

int getSocketByBTAddr(int *socket, const char *btAddress);

int isConnected();

#endif /* DEVICESMANAGER_H_ */


