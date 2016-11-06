
package net.kaisoz.droidstorm.nxt.handler;

import net.kaisoz.droidstorm.R;
import net.kaisoz.droidstorm.bluetooth.exception.BluetoothException;
import net.kaisoz.droidstorm.nxt.controller.NXTButtonController;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which implements the robot control by buttons.
 * It presents a joypad-like set of buttons which all of them are associated to a basic movement (go forwards, backwards, turn around left, or turn around right)
 *
 * @author Tomás Tormo Franco
 */
public class ButtonHandlerActivity extends NXTHandlerBaseActivity implements SeekBar.OnSeekBarChangeListener {

    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int FORWARD = 3;
    private static final int BACKWARDS = 4;
    private static int CURRENT_ACTION = -1;

    private ImageButton mGoForward;
    private ImageButton mGoBackwards;
    private ImageButton mGoLeft;
    private ImageButton mGoRight;
    private ImageButton mStop;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(R.string.activity_label_butthandler);
        setContentView(R.layout.buttons_handler);
        mController = new NXTButtonController();
        mController.setPower(70);

        SeekBar seekBar = (SeekBar) findViewById(R.id.button_handler_progress);
        seekBar.setOnSeekBarChangeListener(this);

        final ImageButton recordingButton = (ImageButton) findViewById(R.id.button_handler_record);
        recordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mRecording) {
                    stopRecording();
                } else {
                    startRecording();
                    Toast.makeText(ButtonHandlerActivity.this, R.string.ButtonHandler_StopRecording, Toast.LENGTH_LONG).show();
                }
            }
        });

        mGoForward = (ImageButton) findViewById(R.id.butt_handler_forward);
        mGoForward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ((NXTButtonController) mController).goForward();
                } catch (Exception e) {
                    handleException(e);
                }
                CURRENT_ACTION = FORWARD;
            }
        });

        mGoBackwards = (ImageButton) findViewById(R.id.butt_handler_back);
        mGoBackwards.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ((NXTButtonController) mController).goBackwards();
                } catch (Exception e) {
                    handleException(e);
                }
                CURRENT_ACTION = BACKWARDS;
            }
        });

        mGoLeft = (ImageButton) findViewById(R.id.butt_handler_left);
        mGoLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ((NXTButtonController) mController).goLeft();
                } catch (Exception e) {
                    handleException(e);
                }
                CURRENT_ACTION = LEFT;
            }
        });

        mGoRight = (ImageButton) findViewById(R.id.butt_handler_right);
        mGoRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ((NXTButtonController) mController).goRight();
                } catch (Exception e) {
                    handleException(e);
                }
                CURRENT_ACTION = RIGHT;
            }
        });

        mStop = (ImageButton) findViewById(R.id.butt_handler_stop);
        mStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    mController.stop();
                } catch (Exception e) {
                    handleException(e);
                }
                CURRENT_ACTION = -1;
            }
        });
        Toast.makeText(ButtonHandlerActivity.this, R.string.ButtonHandler_StartRecording, Toast.LENGTH_LONG).show();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setLocale(R.string.activity_label_butthandler);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        mController.setPower(progress);
        TextView powerText = (TextView) findViewById(R.id.butt_handler_pow);
        powerText.setText(String.valueOf(progress));

        try {
            switch (CURRENT_ACTION) {
                case FORWARD:
                    ((NXTButtonController) mController).goForward();
                    break;
                case BACKWARDS:
                    ((NXTButtonController) mController).goBackwards();
                    break;
                case LEFT:
                    ((NXTButtonController) mController).goLeft();
                    break;
                case RIGHT:
                    ((NXTButtonController) mController).goRight();
                    break;
            }
        } catch (BluetoothException e) {
            handleException(e);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    @Override
    protected boolean disableHandler() {
        mGoForward.setEnabled(false);
        mGoBackwards.setEnabled(false);
        mGoLeft.setEnabled(false);
        mGoRight.setEnabled(false);
        mStop.setEnabled(false);
        try {
            mController.stop();
        } catch (Exception e) {
            handleException(e);
        }
        return true;
    }

    @Override
    protected boolean enableHandler() {
        mGoForward.setEnabled(true);
        mGoBackwards.setEnabled(true);
        mGoLeft.setEnabled(true);
        mGoRight.setEnabled(true);
        mStop.setEnabled(true);
        return true;
    }


}