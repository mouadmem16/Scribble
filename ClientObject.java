import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientObject {
    private String ip;
    private int port;
    private Consumer<Data> onRecieveCallBack;
    private ConnectionThread connectionThread = new ConnectionThread();

    public ConnectionThread getConnectionThread() {
        return connectionThread;
    }

    public ClientObject(int port, String ip, Consumer<Data> onRecieveCallBack) {
        this.onRecieveCallBack = onRecieveCallBack;
        connectionThread.setDaemon(true);
        this.port = port;
        this.ip = ip;
        connectionThread.start();
    }

    public void send(Serializable data){
        try {
            connectionThread.out.writeObject(data);
        }catch(Exception i){}
    }

    public void closeConnection(){
        try{
            connectionThread.socket.close();
        }catch(Exception i){}
    }

    protected String getIp() {
        return ip;
    }

    protected int getPort() {
        return port;
    }

    class ConnectionThread extends Thread{
        Socket socket;
        ObjectOutputStream out;

        @Override
        public void run(){
            try(Socket socket =  new Socket(getIp(), getPort());
                ObjectOutputStream out  = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

                this.out = out;
                this.socket = socket;
                socket.setTcpNoDelay(true);

                while (true){
                    Data data = (Data) in.readObject();
                    onRecieveCallBack.accept(data);
                }
            }catch(Exception i){
                System.out.println(i.getMessage());
            }
        }
    }
}


class Data implements Serializable{
    String color, type, state;
    Double x,y, width;

    public Data(String color, String type, String state, Double width, Double x, Double y) {
        this.color = color;
        this.state = state;
        this.type = type;
        this.width = width;
        this.x = x;
        this.y = y;
    }
}