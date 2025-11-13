package MotionControl;

public class Virtual_Motor {
    //If the motor is on or not
    private boolean on;
    //The direction the motor is running
    private Direction direction;
    public Virtual_Motor(){
        this.on = false;
        this.direction = Direction.NULL;
    }
    /**
     * Start API for the motor, starts the motor
     */
    public synchronized void start(){
        //Notify Software Bus, wait for response to turn on
    }
    /**
     * Stop API for the motor, stops the motor
     */
    public synchronized void stop(){
        //Notify Software Bus, wait for response to turn on
    }

    /**
     * Set direction API for the motor, sets direction
     * @param direction
     */
    public synchronized void set_direction(Direction direction){
        //Notify Software Bus, wait for response to set direction
    }

    /**
     * Gets the direction of the motor
     * @return
     */
    public Direction get_direction(){
        return this.direction;
    }

    /**
     * Lets us check if the motor is on or off.
     * Used to snap into place in motion sim
     * @return true if the motor is on
     */
    public boolean is_off(){
        return !on;
    }
}
