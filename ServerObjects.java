import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerObjects {
    static List<ClientHandelObject> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException{
        Socket socket;
        ServerSocket serverSocket = new ServerSocket(9998);
        while (true)
        {
            socket = serverSocket.accept();
            System.out.println("Connected !! ");
            ClientHandelObject ch = new ClientHandelObject(socket);
            ch.start();
            clients.add(ch);
        }
    }

    public static void notifier(String id, Object message){
        clients.forEach((clientHandel) -> {
            if(!clientHandel.id.equals(id)){
                clientHandel.sendmessage(message);
            }
        });
    }
}

class ClientHandelObject extends Thread{
    String id;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    Socket socket;

    public ClientHandelObject(Socket socket) throws IOException {
        dis = new ObjectInputStream(socket.getInputStream());
        dos = new ObjectOutputStream(socket.getOutputStream());
        this.id = socket.getRemoteSocketAddress().toString();
        this.socket = socket;
    }

    public void sendmessage(Object msg){
        try{
            dos.writeObject(msg);
        }catch(Exception ignored){}
    }

    @Override
    public void run() {
        Object message;
        while (true){
            try {
                message = dis.readObject();
                ServerObjects.notifier(id, message);
            } catch (Exception ignored) {
                try {
                    socket.close();
                    this.stop();
                } catch (IOException e) {}
            }
        }
    }
}
