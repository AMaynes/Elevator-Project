package MotionControl;

import java.util.ArrayList;
import java.util.List;

public class MotionAPI {
    public Direction direction=null;
    private Integer top_Alignment=null;
    private Integer bottom_Alignment=null;

    private List<Virtual_Sensor> sensors = new ArrayList<>();
    private Virtual_Motor motor = new Virtual_Motor();


    /**
     * Set directions of elevator
     * @param direction Up, down or null
     */
    public void set_direction(Direction direction){
        motor.set_direction(direction);
    }

    /**
     * Returns the floor that the top of elevator is aligned with
     * @return A floor number or null
     */
    public Integer top_alignment(){
        for (Virtual_Sensor s : sensors) {
            if (s.is_triggered() && sensors.indexOf(s) % 2 == 1) return sensors.indexOf(s);
        }
        return null;
    }

    /**
     * Returns the floor that the bottom of elevator is aligned with
     * @return A floor number or null
     */
    public Integer bottom_alignment(){
        for (Virtual_Sensor s : sensors) {
            if (s.is_triggered() && sensors.indexOf(s) % 2 == 0) return sensors.indexOf(s);
        }
        return null;
    }

    /**
     *Starts the motor and the elevator’s
     * movement at constant_speed either up or down
     */
    public void start(){
        motor.start();
    }

    /**
     *Starts the motor and the elevator’s
     * movement from constant_speed to zero with a decay of deeleration
     */
    public void stop(){
        motor.stop();
    }
}
