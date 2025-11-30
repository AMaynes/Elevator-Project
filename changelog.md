# Changelog
A little late but starting a changelog because thats pretty cool.

## History


11/25/2025 A. Maynes  
```
--------
All from the todo list:
* Write handleCarStop(msg) in ElevatorMultiplexor.java.
  * Stops the motor.
* Adjust handleCarDispatch(msg) in ElevatorMultiplexor.java.
  * Only starts the motor in the given direction.
  * Doesn't care about the target floor.
* Adjust pollCarPosition() in ElevatorMultiplexor.java.
    * Check newFloor != currentFloor in order to update the GUI.
    * Remove the arrival logic.
    * Send out messages according to Sensor readings. **READ NEXT ITEM FOR DETAILS**
* Send out Top Sensor Trigger (210) and Bottom Sensor Trigger (211) when polling location in ElevatorMultiplexor.java.
    * Body is the Sensor's index, subtopic is the elevID.
    * Only send out when top_sensor_triggered()/bottom_sensor_triggered() don't return null.
* Send out currDirection and currDirection Messages upon Motor start() and stop().
Luxury Updates:
* Resize the GUI to a better size.
* Show the floor numbers next to the floor call buttons, or when hovering over one.
--------
```
- Rewrote pollCarPosition so that it used both sensors and published them when they changed. Uses a past state variable to compare to.
- Implemented GUI resizing based on users screen resolution. Note: adjusting the scale does not work well with the label positions, but hopefully will work with varying screen resolutions still.
- Wrote handleStopCar function
- Implemented fireAlarm handling in the elevator MUX because it was the easiest way to clear button selections during fire modes as a TODO requested be done.
- Note: There is a bug wher you need to stop the elevator twice for some reason for it to fully take. Might be linked to the ambiguous duplicate bus command printing. Not sure though.
---