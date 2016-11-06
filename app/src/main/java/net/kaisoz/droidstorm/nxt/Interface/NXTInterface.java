package net.kaisoz.droidstorm.nxt.Interface;

/**
 * Holds NXT interaction constants
 *
 * @author Tomás Tormo Franco
 */
public class NXTInterface {

    // Message constants
    public static final char MESSAGETYPE_RESPONSE = 0x0000;
    public static final char MESSAGETYPE_NORESPONSE = 0x8000;
    public static final char RESPONSE_SUCCESS = 0x0000;
    protected static char COMMAND_BYTE = 0xFFFF;


    // MotorInterface Constants */
    public static final char NO_MOTOR = 0x0300;
    public static final int LEFT_MOTOR = 0;
    public static final int RIGHT_MOTOR = 1;

    // Motor port constanst
    public static final char MOTOR_A = 0x0000;
    public static final char MOTOR_B = 0x0100;
    public static final char MOTOR_C = 0x0200;
    public static final char MOTOR_ALL = 0xFF00;

    // Tacholimit constants
    public static long TACHO_MAX_VALUE = 4294967295L;
    public static long TACHO_MIN_VALUE = 0;

    // Motor mode constants
    public static final char MOTOR_MODE_COAST = 0x0000;
    public static final char MOTOR_MODE_ON = 0x0100;
    public static final char MOTOR_MODE_BRAKE = 0x0200;
    public static final char MOTOR_MODE_REGULATED = 0x0400;
    public static final char MOTOR_MODE_ON_BRAKE = (0x0100 | 0x0200);
    public static final char MOTOR_MODE_ON_REGULATED = (0x0100 | 0x0400);
    public static final char MOTOR_MODE_ON_REGULATED_BRAKE = (0x0100 | 0x0400 | 0x0200);

    // Motor regulation constants
    public static final char MOTOR_REGULATION_IDLE = 0x0000;
    public static final char MOTOR_REGULATION_MOTORSPEED = 0x0001;
    public static final char MOTOR_REGULATION_MOTORSYNC = 0x0002;

    // Motor runstate constants
    public static final char MOTOR_RUNSTATE_IDLE = 0x0000;
    public static final char MOTOR_RUNSTATE_RAMPUP = 0x0010;
    public static final char MOTOR_RUNSTATE_RUNNING = 0x0020;
    public static final char MOTOR_RUNSTATE_RAMPDOWN = 0x0002;
}
