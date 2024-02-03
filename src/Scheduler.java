/**
 * This Class represents the Scheduler which acts as a communication line
 * to pass messages between Floor and Elevator Subsystems.
 * @author Mahnoor Fatima
 */
public class Scheduler implements Runnable {
    //MessageBoxes for communication with Elevator and Floor Threads
    MessageBox incomingFloor, outgoingFloor, incomingElevator, outgoingElevator;

    /**
     * Constructor for class Scheduler.
     * @param box1 incoming MessageBox with Floor
     * @param box2 outgoing MessageBox with Floor
     * @param box3 incoming MessageBox with Elevator
     * @param box4 outgoing MessageBox with Elevator
     */
    public Scheduler(MessageBox box1, MessageBox box2, MessageBox box3, MessageBox box4) {
        incomingFloor = box1;
        outgoingFloor = box3;
        incomingElevator = box2;
        outgoingElevator = box4;

    }

    /**
     * Get Message from Floor and send it to Elevator
     * @return the message received from the Floor MessageBox, null if empty
     */
    public Message checkFloorBox(){
        Message floorMessage = incomingFloor.get();
        if (floorMessage == null){
            return null;
        }
        System.out.println(Thread.currentThread().getName() + " received message from Floor : " + floorMessage);
        outgoingFloor.put(floorMessage);
        System.out.println(Thread.currentThread().getName() + " sent message to Elevator : " + floorMessage);
        return floorMessage;
    }

    /**
     * Get Message from Elevator and send it to Floor
     * @return @return the message received from the Elevator MessageBox, null if empty
     */
    public Message checkElevatorBox(){
        Message elevatorMessage = outgoingElevator.get();
        if (elevatorMessage == null){
            return null;
        }
        System.out.println(Thread.currentThread().getName() + " received message from Elevator : " + elevatorMessage);
        incomingElevator.put(elevatorMessage);
        System.out.println(Thread.currentThread().getName() + " sent message to Floor : " + elevatorMessage);
        return elevatorMessage;
    }


    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            checkFloorBox();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            Message elevatorMessage = checkElevatorBox();
            if (elevatorMessage == null){
                System.out.println("Scheduler System Exited");
                return;
            }

        }
    }
}