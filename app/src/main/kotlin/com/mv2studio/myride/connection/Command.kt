package com.mv2studio.myride.connection

import com.github.pires.obd.commands.ObdCommand
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.control.DistanceSinceCCCommand
import com.github.pires.obd.commands.engine.OilTempCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.engine.ThrottlePositionCommand
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand
import com.github.pires.obd.commands.fuel.FuelLevelCommand
import com.github.pires.obd.commands.fuel.FuelTrimCommand
import com.github.pires.obd.commands.protocol.*
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand
import com.github.pires.obd.enums.FuelTrim
import com.github.pires.obd.enums.ObdProtocols.AUTO

/**
 * Created by matej on 15/11/2016.
 */
class Command(val commandType: CommandType) {

    val obdCommand: ObdCommand = getNewObdCommand()

    @CommandState
    var state = State.NEW

    private fun getNewObdCommand(): ObdCommand {
        when (commandType) {
            CommandType.RPM -> return RPMCommand()
            CommandType.THROTTLE_POSITION -> return ThrottlePositionCommand()
            CommandType.CONSUMPTION_RATE -> return ConsumptionRateCommand()
            CommandType.TEMPERATURE_OIL -> return OilTempCommand()

        // ELM does not provide ODO. With little workaround, we can keep track of odo if user occasionally provides it manually.
        // When this happens, keep track of DISTANCE_SINCE_CC and once this is resets, let user note that ODO in the app may not be correct.
            CommandType.DISTANCE_SINCE_CC -> return DistanceSinceCCCommand()
            CommandType.TEMPERATURE_AMBIENT_AIR -> return AmbientAirTemperatureCommand()
            CommandType.TEMPERATURE_INTAKE -> return AirIntakeTemperatureCommand()
            CommandType.SPEED -> return SpeedCommand()
            CommandType.FUEL_TRIM -> return FuelTrimCommand(FuelTrim.LONG_TERM_BANK_1)
            CommandType.FUEL_LEVEL -> return FuelLevelCommand()
            CommandType.ODB_RESET -> return ObdResetCommand()
            CommandType.ECHO_OFF -> return EchoOffCommand()
            CommandType.LINE_FEED_OFF -> return LineFeedOffCommand()
            CommandType.TIMEOUT -> return TimeoutCommand(62)
            CommandType.SELECT_PROTOCOL -> return SelectProtocolCommand(AUTO)
        }
    }


}