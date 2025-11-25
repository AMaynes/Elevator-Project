NOTE ALL TODOS IN THE ACTUAL CODE.

TODO:



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

* TESTING!
* Integrate elevatorController code.
  * Most devices are unwritten.
* Write Main.

Luxury Updates:
* Resize the GUI to a better size. 
* Show the floor numbers next to the floor call buttons, or when hovering over one.
