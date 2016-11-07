package com.mv2studio.myride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.mv2studio.myride.connection.CommandType;
import com.mv2studio.myride.connection.ConnectionManager;
import com.mv2studio.myride.utils.Log;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.dataText) TextView mDataTextView;
    @BindView(R.id.statusText) TextView mStatusTextView;

    private ConnectionManager mConnectionManager;
    private Disposable mCommandDisposable;
    private Disposable mConnectionDisposable;

    private String lastRpm, consumptionRate, fuelLevel, distance;
    private CommandType[] mCommands = {CommandType.RPM, CommandType.CONSUMPTION_RATE,
            CommandType.FUEL_LEVEL, CommandType.DISTANCE_SINCE_CC};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mConnectionManager = ConnectionManager.getInstance();

        mStatusTextView = (TextView) findViewById(R.id.statusText);
        mDataTextView = (TextView) findViewById(R.id.dataText);
        findViewById(R.id.startTrackingButton).setOnClickListener(view -> {
            mConnectionManager.reconnect();
        });

        mConnectionDisposable = mConnectionManager.registerForStateUpdates(state -> {
            mStatusTextView.setText(state.name());
            Log.d("Ativity received new state " + state.name());
        });

        mCommandDisposable = mConnectionManager.registerForCommandUpdates(command -> {
            switch (command.getCommandType()) {
                case RPM:
                    lastRpm = command.getObdCommand().getFormattedResult();
                    break;
                case CONSUMPTION_RATE:
                    consumptionRate = command.getObdCommand().getFormattedResult();
                    break;
                case FUEL_LEVEL:
                    fuelLevel = command.getObdCommand().getFormattedResult();
                    break;
                case DISTANCE_SINCE_CC:
                    distance = command.getObdCommand().getFormattedResult();
                    break;
            }

            final String text = "rpm " + lastRpm + "\nconsumption rate " + consumptionRate + "\nfuel level " + fuelLevel + "\ndistance since cc " + distance;
            mDataTextView.setText(text);
        }, mCommands);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCommandDisposable.dispose();
        mConnectionDisposable.dispose();
    }

    @OnClick(R.id.startTrackingButton)
    public void onStopService() {
        mConnectionManager.reconnect();
    }
}
