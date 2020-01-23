import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClientChat {
    private String ip;
    private int port;
    String Name;
    private Consumer<Serializable> onRecieveCallBack;
    private ConnectionThread connectionThread = new ConnectionThread();

    public ClientChat(int port, String ip, String name, Consumer<Serializable> onRecieveCallBack) {
        this.onRecieveCallBack = onRecieveCallBack;
        connectionThread.setDaemon(true);
        Name = name;
        this.port = port;
        this.ip = ip;
    }

    public void startConnection(){
        connectionThread.start();
    }

    public void send(Serializable data){
        try {
            connectionThread.out.writeObject(data);
        }catch(Exception i){}
    }

    public void closeConnection(){
        try{
            connectionThread.out.writeObject("<logout>");
            connectionThread.socket.close();
        }catch(Exception i){}
    }

    protected String getIp() {
        return ip;
    }

    protected int getPort() {
        return port;
    }

    public static Map<String, String> parseMessage(String str){
        Map<String, String> res = new HashMap<>();
        res.put("name", str.split("<name>")[1]);
        if(str.contains("<msg>"))
            res.put("msg", str.split("<msg>")[1]);
        return res;
    }

    private class ConnectionThread extends Thread{
        private Socket socket;
        private ObjectOutputStream out;

        @Override
        public void run(){
            try(Socket socket =  new Socket(getIp(), getPort());
                ObjectOutputStream out  = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

                this.out = out;
                this.socket = socket;
                socket.setTcpNoDelay(true);
                out.writeObject("<name>"+Name+"<name>");
                while (true){
                    Serializable data = (Serializable) in.readObject();
                    onRecieveCallBack.accept(data);
                }
            }catch(Exception i){
                System.out.println(i.getMessage());
            }
        }
    }
}