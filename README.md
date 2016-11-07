# Table of Contents

[**DroidStorm**](#droidstorm)
* [**Synchronization mode**](#synchronization-mode)
* [**Follower mode**](#follower-mode)
* [**Demo mode**](#demo-mode)

[**Notes**](#notes)

# DroidStorm

Droidstorm is an Android app which allows the user to control up to seven Bluetooth-enabled LEGO MINDSTORMS NXT robots with one single phone. The app should **work in phones running Android versions from Cupcake to Nougat**.

It provides three main operating modes: synchronization mode, follower mode and demo mode.

Synchronization mode
--------------------

In synchronization mode, the phone controls up to seven connected robots, sending the same movement commands to all of them. This mode offers two different human interfaces to control de robots.

One of them allows the use of the phone as a joystick, thanks to the orientation sensors the smartphones include. 
The second interface presents a joypad-like set of buttons, which the user can use to send basic orders to the robot as move forwards, move backwards, turn left or turn right.

Click the image to watch the synchronization mode in action:

[![Sychronized mode](https://img.youtube.com/vi/HoDeFzRnQ6U/0.jpg)](https://www.youtube.com/watch?v=HoDeFzRnQ6U)


Follower mode
-------------

In follower mode, a leader/follower approach is established. One of the robots becomes the leader and the rest become followers. The leader robot is the only one controlled by the phone and the followers follow leader's path.

For the leader, a modulated infrared emitter has been specially built for that purpose. This infrared emitter is used as a point of reference for the followers. The followers use an infrared sensor capable of filtering modulated signals and has been programmed in order to use data received by the sensor to be able to follow the leader.


Click the image to watch the follower mode in action:

[![Follower mode](https://img.youtube.com/vi/v6kLUdtcLiQ/0.jpg)](https://www.youtube.com/watch?v=v6kLUdtcLiQ)


Demo mode
---------

Demo mode offers the possibility of recording movements. The user can perform several movements with the robots and record them in order to reproduce them later.

This mode allows to use a robot as, for example, surveillance robot, covering one space just once and making it repeat it as many times as wanted without user interaction. Furthermore, a predefined sets of movements from XML files can be used. This XML files can be created in external devices (such as computers) and loaded later in the application to make the robot move as defined.


NOTES
=====

First of all...**THIS PROJECT IS UNDER MAINTENANCE**.

This app is part of my Master Thesis I wrote in 2010 (Froyo just appeared on market). The interface is ugly and old as hell, and there probably are too many bugs.
Moreover, since it had to support Cupcake (which lacked Bluetooth API), I had to write my own Bluetooth interface.

I modified it to also support the Android Bluetooth API so far. The app detects the phone's Android version and uses the appropriate Bluetooth interface.
However there a lot of work to do:

* Build a fresh and new interface
* Bug hunting
* Code refactoring
* Improve documentation
* Testing!! (Unit tests, UI tests...)
