import java.io.IOException;
import java.net.*;

/**
 * This class represents the Elevator Subsystem which receives scheduler commands and distributes
 * them to the respective elevator threads. The subsystem periodically updates
 * the scheduler with the live ElevatorData for all threads.
 * @author Mahnoor Fatima 101192353
 * @author Owen Peterson 101233850
 */
public class ElevatorSubsystem extends CommunicationRPC implements Runnable{

    //Message boxes for communication with Scheduler
    private MessageBox incomingMessages, outgoingMessages;

    private Elevator[] elevators;

    private MessageBox[] messageBoxes;
    private ElevatorData elevatorData;
    private static final int ELEVATOR_PORT = 23;

    /**
     * Constructor for class ElevatorSubsystem
     *
     * @param box3 Incoming messages MessageBox
     * @param box4 Outgoing messages MessageBox
     */
    public ElevatorSubsystem(Integer numElevators, Integer numFloors, MessageBox box3, MessageBox box4) {
        super(ELEVATOR_PORT);
        this.incomingMessages = box3;
        this.outgoingMessages = box4;

        elevatorData = new ElevatorData();
        elevators = new Elevator[numElevators];


        ElevatorView view = new ElevatorView();

        for(int i =0; i < numElevators; i++){
            elevators[i] = new Elevator(i, numFloors, elevatorData, view);
            (new Thread(elevators[i])).start();
        }


        (new ElevatorUpdateSender()).start();

    }
    @Override
    public void run() {
        while(true){
            receiveAndSend(elevatorData.toByteArray());

            byte command[] = receiveSendPacket.getData();

            //read the first byte of the command, which is elevator id
            int elevatorId = command[0];

            byte[] byteMessage = new byte[command.length - 1];

            for(int i = 1; i < command.length; i++){
                byteMessage[i-1] = command[i];
            }
            if (byteMessage == null) {
                System.out.println("Elevator System Exited");
                outgoingMessages.put(null);
                return;
            }
            Message message = new Message(byteMessage);



            System.out.println(Thread.currentThread().getName() + " received message from Scheduler : " + message);

            //send message to correct elevator
            elevators[elevatorId].giveRequest(message);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

    }

    public Elevator getElevator(int i) {
        return elevators[i];
    }

    class ElevatorUpdateSender extends Thread {
        private DatagramPacket sendUpdatePacket;
        private DatagramSocket sendUpdateSocket;
        public ElevatorUpdateSender(){
            try {
                sendUpdateSocket = new DatagramSocket();
            } catch (SocketException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        public void run(){
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e){
                    e.printStackTrace();
                    System.exit(1);
                }

                byte[] data = elevatorData.toByteArray();

                try {
                    sendUpdatePacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 65);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
//                System.out.println("ElevatorSubsystem: sending update data...");

                try {
                    sendUpdateSocket.send(sendUpdatePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                byte receiveData[] = new byte[100];
                DatagramPacket schedulerAckPacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    sendUpdateSocket.receive(schedulerAckPacket);
//                    System.out.println("ElevatorSubsystem: update acknowledgement received");
                }catch (IOException e){
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread elevator;
        MessageBox box1, box2;

        box1 = new MessageBox(); //incomingElevator box
        box2 = new MessageBox(); //outgoingElevator bpx
        elevator = new Thread(new ElevatorSubsystem( 4, 22, box1, box2),"ElevatorSubsystem");
        elevator.start();
    }
}