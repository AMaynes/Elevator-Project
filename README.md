# Elevator Project
Ding-dong; you have reached the: CS460 Elevator Project, System Design.

## Dependencies
- Java 21 (or a compatible JDK)
- JavaFX available on the classpath/module-path

## How to run the demo
- Compile all java files
- Run the file Main.java

## Notes/TODO
- In the debugging stage.
- Doors need to open upon an Elevator turning on. Should only close when it has a request to service.
- Needs to play sounds.
- Needs to react to call button presses properly.
- NUMEROUS OTHER ISSUES
- Acceptable commands for the MUXs:
```
Topic | Meaning           |Subtopic (1 int)| Body (1 int) | Meaning of Body   | Receiving Device | Sending Device
------------------------------------------------------------------------------------------------------------------
100	  |Door control	      |1,2,3,4 (elevID)| 0/1	      |0=open 1=close	  | Elevator MUX	 | DoorAssembly
102	  |Car dispatch	      |1,2,3,4 (elevID)| 0/1	      |0=up 1=down	      | Elevator MUX     | Cabin
103	  |Car stop	          |1,2,3,4 (elevID)| 0	          |stop car	          | Elevator MUX     | Cabin
109	  |Selection reset	  |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Elevator MUX     | Buttons
110	  |Call reset	      |5               |(0-9)0/(0-9)1 |0=up 1=down 1st=flr| Building MUX     | Buttons
111	  |Display floor	  |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Elevator MUX     | Cabin
112	  |Display direction  |1,2,3,4 (elevID)| 0/1/2	      |0=up 1=down 2=none | Elevator MUX     | Cabin
113	  |Calls Enabled	  |5	           | 0/1	      |0=disabled1=enabled| Building MUX     | Buttons
114	  |Selections Enabled |1,2,3,4 (elevID)| 0/1	      |0=disabled1=enabled| Elevator MUX     | Buttons
115	  |Selections type	  |1,2,3,4 (elevID)| 0/1	      |0=single1=multiple |	Elevator MUX     | Buttons
116	  |Play sound	      |1,2,3,4 (elevID)| 0/1          |0=arrival1=overload| Elevator MUX     | Notifier
120	  |Fire Alarm	      |1,2,3,4,5	   | 0/1	      |0=off 1=on         | Building MUX     | Mode

200	  |Hall call	      |1,2,3,4 (elevID)| 101-110,1-10 |>100=up <100=down  | Buttons	         | Building MUX
201	  |Cabin select	      |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Buttons	         | Elevator MUX
202	  |Car position	      |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Cabin	         | Elevator MUX
203	  |Door sensor	      |1,2,3,4 (elevID)| 0/1	      |0=obstructed1=clear| Door Assembly	 | Elevator MUX
204	  |Door status	      |1,2,3,4 (elevID)| 0/1	      |0=open 1=closed    | Door Assembly	 | Elevator MUX
205	  |Cabin load	      |1,2,3,4 (elevID)| 0/1	      |0=normal1=overload | Cabin            | Elevator MUX
206	  |Fire Key	          |1,2,3,4 (elevID)| 0/1	      |0=inactive1=active | Mode	         | Elevator MUX	
207	  |Car direction	  |1,2,3,4 (elevID)| 0/1/2	      |0=up1=down2=none   | Cabin	         | Elevator MUX
208	  |Car movement	      |1,2,3,4 (elevID)| 0/1	      |0=idle 1=moving	  | Cabin	         | Elevator MUX
209	  |Fire alarm active  |5	           | 0/1	      |0=idle 1=pulled	  | Mode	         | Building MUX
210	  |Top Sensor trig.   |1,2,3,4 (elevID)| 0-19 odd	  |Sensor ID	      | Cabin	         | Elevator MUX
211	  |Bottom Sensor trig.|1,2,3,4 (elevID)| 0-19 even	  |Sensor ID	      | Cabin	         | Elevator MUX
```


