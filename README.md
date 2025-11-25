# Elevator-Project

A small Java/JavaFX API that simulates a set of elevator passenger devices
and a simple GUI that updates images when model events occur. It is intended to
be part of a much larger project.

## Dependencies
- Java 21 (or a compatible JDK)
- JavaFX available on the classpath/module-path

## File structure
```
Elevator-Project/
├─ docs/
├─ res/
│  ├─ sounds/ (.mp3)
│  └─ image_files (.png)
├─ src/
│ 	├─ bus/
│ 	    ├─ Bus/
│ 		│   └─ SoftwareBusCodes.java
│ 		└─ Message/
│ 	├─ center/
│ 	    ├─ TestCommandCenterDisplay.java
│ 		└─ TestCommandCenterMain.java    # Command center that takes in user Messages
│ 	├─ motion/
│ 	    └─ MotionAPI.java
│ 	├─ mux/
│ 	    ├─ BuildingMultiplexor.java
│ 		└─ ElevatorMultiplexor.java
│ 	├─ pfdAPI/
│ 	    ├─ Building.java
│       ├─ CabinPassengerPanel.java
│       ├─ CabinPassengerPanelAPI.java
│       ├─ Elevator.java
│       ├─ ElevatorDoorsAssembly.java
│       ├─ ElevatorFloorDisplay.java
│ 		└─ FloorCallButtons.java
│ 	├─ pfdGUI/
│ 		└─ gui.java    # JavaFX GUI with listeners in the api backend files
│ 	├─ utils/
│ 		├─ imageLoader.java
│ 		└─ testImageSort.java
│ 	└─ Main.java
├─ .gitignore
├─ README.md
├─ TODO.md
```

## How to run the demo
- Compile all java files
- Run the file TestCommandCenterMain.java
- Run the file gui.java

`gui.java` starts the GUI and then simulates calls like pressing floor buttons and opening/closing doors. 
The GUI swaps images from `res/` to show the current state.

## Notes
- In order to play sounds, MARK res/ AS RESOURCE FOLDER.
- Commands for input in TCC must be in the format int-int-int, corresponding to topic-subtopic-body. Acceptable commands:
```
Topic | Meaning           |Subtopic (1 int)| Body (1 int) | Meaning of Body   | Receiving Device | Sending Device
------------------------------------------------------------------------------------------------------------------
100	  |Door control	      |1,2,3,4 (elevID)| 0/1	      |0=open 1=close	  | Elevator MUX	 | DoorAssembly
102	  |Car dispatch	      |1,2,3,4 (elevID)| 0/1	      |0=up 1=down	      | Elevator MUX     | Cabin
103	  |Car stop	          |1,2,3,4 (elevID)| 0	          |stop car	          | Elevator MUX     | Cabin
109	  |Selection reset	  |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Elevator MUX     | Buttons
110	  |Call reset	      |1 to 10 (floor#)| 0/1	      |0=up 1=down        | Building MUX     | Buttons
111	  |Display floor	  |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Elevator MUX     | Cabin
112	  |Display direction  |1,2,3,4 (elevID)| 0/1/2	      |0=up 1=down 2=none | Elevator MUX     | Cabin
113	  |Calls Enabled	  |0	           | 0/1	      |0=disabled1=enabled| Building MUX     | Buttons
114	  |Selections Enabled |1,2,3,4 (elevID)| 0/1	      |0=disabled1=enabled| Elevator MUX     | Buttons
115	  |Selections type	  |1,2,3,4 (elevID)| 0/1	      |0=single1=multiple |	Elevator MUX     | Buttons
116	  |Play sound	      |1,2,3,4 (elevID)| 0/1          |0=arrival1=overload| Elevator MUX     | Notifier
120	  |Fire Alarm	      |0	           | 0/1	      |0=on 1=off         | Building MUX     | Mode

200	  |Hall call	      |1 to 10 (floor#)| 0/1	      |0=up 1=down     	  | Buttons	         | Building MUX
201	  |Cabin select	      |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Buttons	         | Elevator MUX
202	  |Car position	      |1,2,3,4 (elevID)| 1-10	      |Floor number	      | Cabin	         | Elevator MUX
203	  |Door sensor	      |1,2,3,4 (elevID)| 0/1	      |0=obstructed1=clear| Door Assembly	 | Elevator MUX
204	  |Door status	      |1,2,3,4 (elevID)| 0/1	      |0=open 1=closed    | Door Assembly	 | Elevator MUX
205	  |Cabin load	      |1,2,3,4 (elevID)| 0/1	      |0=normal1=overload | Cabin            | Elevator MUX
206	  |Fire Key	          |1,2,3,4 (elevID)| 0/1	      |0=inactive1=active | Mode	         | Elevator MUX	
207	  |Car direction	  |1,2,3,4 (elevID)| 0/1/2	      |0=up1=down2=none   | Cabin	         | Elevator MUX
208	  |Car movement	      |1,2,3,4 (elevID)| 0/1	      |0=idle 1=moving	  | Cabin	         | Elevator MUX
209	  |Fire alarm active  |0	           | 0/1	      |0=idle 1=pulled	  | Mode	         | Building MUX
210	  |Top Sensor trig.   |1,2,3,4 (elevID)| 0-19 odd	  |Sensor ID	      | Cabin	         | Elevator MUX
209	  |Bottom Sensor trig.|1,2,3,4 (elevID)| 0-19 even	  |Sensor ID	       | Cabin	         | Elevator MUX
```
