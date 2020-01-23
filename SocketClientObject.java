import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class SocketClientObject extends Thread{
    /*--------------ELEMENTS OF the Person----------------*/
    String Name;

    /*--------------ELEMENTS OF the Socket----------------*/
    Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    public SocketClientObject(int port, String name){
        this.Name = name;
        try {
            socket = new Socket("localhost", port);
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ignored) {}

/*        new Thread(new Runnable(){
            @Override
            public void run() {
                Test message;
                while (true){
                    try {
                        message = (Test) ois.readObject();
                    } catch (IOException | ClassNotFoundException ignored) {}
                }
            }
        }).start();*/
    }

    @Override
    public void run() {
        while (true) {
            Scanner scn = new Scanner(System.in);
            SendMessage(scn.next());
        }
    }

    public void SendMessage(String message) {
        try {
            oos.writeUTF("<msg>"+message+"<msg>");
        } catch (Exception ignored) {}
    }

    public void closeSession() {
        try {
            oos.writeUTF("<logout>");
            oos.close();
            ois.close();
            socket.close();
            System.exit(1);
        } catch (Throwable ignored) {}
    }

/*
    public void openSession() {
        try {
            oos.writeUTF("<name>"+Name+"<name>");
        } catch (Exception ignored) {}
    }

    */
/* <name>Mouaad<name>  <msg>salam ba mouaad<msg> <logout> *//*

    public Map<String, String> parseMessage(String str){
        Map<String, String> res = new HashMap<>();
        res.put("name", str.split("<name>")[1]);
        if(str.contains("<msg>"))
            res.put("msg", str.split("<msg>")[1]);

        return res;
    }
*/

    public static void main(String[] args) {
        SocketClientObject cd = new SocketClientObject(9998, "");
        cd.start();
    }
}



