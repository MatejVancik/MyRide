package com.mv2studio.myride.connection;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.FuelTrimCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.FuelTrim;

import static com.github.pires.obd.enums.ObdProtocols.AUTO;

public class Command {

    private CommandType mCommandType;
    private ObdCommand mObdCommand;

    @CommandState
    private int mState;

    Command(CommandType commandType) {
        this.mCommandType = commandType;
        mObdCommand = getNewObdCommand();
        mState = CommandState.NEW;
    }

    private ObdCommand getNewObdCommand() {
        switch (mCommandType) {
            case RPM: return new RPMCommand();
            case THROTTLE_POSITION: return new ThrottlePositionCommand();
            case CONSUMPTION_RATE: return new ConsumptionRateCommand();
            case TEMPERATURE_OIL: return new OilTempCommand();

            // ELM does not provide ODO. With little workaround, we can keep track of odo if user occasionally provides it manually.
            // When this happens, keep track of DISTANCE_SINCE_CC and once this is resets, let user note that ODO in the app may not be correct.
            case DISTANCE_SINCE_CC: return new DistanceSinceCCCommand();
            case TEMPERATURE_AMBIENT_AIR: return new AmbientAirTemperatureCommand();
            case TEMPERATURE_INTAKE: return new AirIntakeTemperatureCommand();
            case SPEED: return new SpeedCommand();
            case FUEL_TRIM: return new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_1);
            case FUEL_LEVEL: return new FuelLevelCommand();
            case ODB_RESET: return new ObdResetCommand();
            case ECHO_OFF: return new EchoOffCommand();
            case LINE_FEED_OFF: return new LineFeedOffCommand();
            case TIMEOUT: return new TimeoutCommand(62);
            case SELECT_PROTOCOL: return new SelectProtocolCommand(AUTO);
        }
        return null;
    }

    public CommandType getCommandType() {
        return mCommandType;
    }

    public ObdCommand getObdCommand() {
        return mObdCommand;
    }

    @CommandState
    public int getState() {
        return mState;
    }

    public void setState(@CommandState int mState) {
        this.mState = mState;
    }
}
