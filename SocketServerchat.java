import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SocketServerchat {
    static ArrayList<ClientHandel> clients = new ArrayList<>();
    static ArrayList<String> words = new ArrayList<>();
    static ControlData word = new ControlData("null");

    static void SendDataClients(){
        ArrayList<Client> names = new ArrayList<>();
        SocketServerchat.clients.forEach(clientHandel -> names.add(new Client(clientHandel.name, clientHandel.Score)));
        notifier("", names);
    }

    public static void main(String[] args) throws IOException
    {
        words.addAll(Arrays.asList(
                "poetry","cooperative","monkey","road","housewife","cancer","requirement","height","baseball","manufacture","person",
                "relation","introduction","personality","sister","garbage","explanation","effort","employer","youth","birthday","quality",
                "writing","housing","actor","presentation","winner","article","solution","hospital","insurance","reputation","meal","administration"
        ));
        Socket socket;
        ServerSocket serverSocket = new ServerSocket(9999);
        while (true)
        {
            socket = serverSocket.accept();
            ClientHandel ch = new ClientHandel(socket);
            ch.start();
            clients.add(ch);
            SendDataClients();
            ch.RunGame();
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

class ClientHandel extends Thread{
    String id;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    Socket socket;
    String name = "Test";
    int Score;
    boolean correctAnswer = false;

    public ClientHandel(Socket socket) throws IOException {
        dis = new ObjectInputStream(socket.getInputStream());
        dos = new ObjectOutputStream(socket.getOutputStream());
        this.id = socket.getRemoteSocketAddress().toString();
        this.socket = socket;
        try { this.name = ((String)dis.readObject()).split("<name>")[1]; } catch(Exception ignored){}
        System.out.println(this.name);
        SocketServerchat.notifier(id, "<name>"+name+"<name>"+"<msg> is Connected :) <msg>");
    }

    public void parseMSG(String str){
        String resStr = "<name>"+name+"<name>";
        if(str.contains("<stop>")){
            RunGame();
        }
            else if(str.contains("<logout>"))
        {
            resStr = resStr.concat("<msg> Left the room :( <msg>");
            SocketServerchat.notifier(id, resStr);
            try {
                socket.close();
                SocketServerchat.clients.remove(this);
                SocketServerchat.SendDataClients();
                this.stop();
            } catch (IOException ignored) {}
        }
            else if(str.contains("<msg>"))
        {
            resStr = resStr.concat("<msg>"+str.split("<msg>")[1]+"<msg>");
            if(verifyWord(str.split("<msg>")[1])) SocketServerchat.notifier(id, resStr);
        }
    }

    void RunGame() {
        if(SocketServerchat.clients.size() < 2){SocketServerchat.word.mot = "null"; return; }
        String lastword = SocketServerchat.word.mot;
        SocketServerchat.word.mot = SocketServerchat.words.get((new Random()).nextInt(SocketServerchat.words.size()));
        ClientHandel Clthand  = SocketServerchat.clients.get((new Random()).nextInt(SocketServerchat.clients.size()));
        while(Clthand.id.equals(id))
            Clthand = SocketServerchat.clients.get((new Random()).nextInt(SocketServerchat.clients.size()));
        SocketServerchat.clients.forEach(clientHandel -> clientHandel.correctAnswer = false);
        Clthand.sendmessage(new ControlData(SocketServerchat.word.mot));
        SocketServerchat.notifier(Clthand.id, new WordData(underWord(), lastword));
    }

    private String underWord() {
        String nom = "";
        for(int i=0; i < SocketServerchat.word.mot.length(); i++){
            if(SocketServerchat.word.mot.charAt(i) == ' ') nom = nom.concat("  ");
            else nom = nom.concat("_ ");
        }
        return nom;
    }

    public boolean verifyWord(String word){
        if(SocketServerchat.word.mot.toLowerCase().equals(word.toLowerCase())){
            if(!correctAnswer) {
                sendMsgAdministration("Jebtiha Las9a, wa drebha b sa9la");
                Score += 100;
                correctAnswer = true;
                SocketServerchat.SendDataClients();
            }
            return false;
        }else return true;
    }

    public void sendmessage(Object msg){
        try{
            dos.writeObject(msg);
        }catch(Exception ignored){}
    }

    public void sendMsgAdministration(String msg){
        try{
            dos.writeObject("<name>Scribble<name><msg>"+msg+"<msg>");
        }catch(Exception ignored){}
    }

    @Override
    public void run() {
        String message;
        while (true){
            try {
                message = (String) dis.readObject();
                System.out.println(message);
                parseMSG(message);
            } catch (Exception ignored) {

            }
        }
    }
}

class ControlData implements Serializable{
    String mot;

    public ControlData(String word) {
        mot = word;
    }
}

class WordData implements Serializable{
    String start;
    String last;

    public WordData(String start, String last) {
        this.start = start;
        this.last = last;
    }
}

class Client implements Serializable{
    String name;
    int score;

    public Client(String name, int score) {
        this.name = name;
        this.score = score;
    }
}

