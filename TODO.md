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
    * Send out messages according to Sensor readings.
* TESTING!
* Integrate elevatorController code.
  * Most devices are unwritten.
* Write Main.

Luxury Updates:
* Resize the GUI to a better size. 
* Show the floor numbers next to the floor call buttons, or when hovering over one.
