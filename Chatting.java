import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class Chatting {
    /*--------------ELEMENTS OF Sockets----------------*/
    ClientChat socketClientchat = null;

    /*--------------ELEMENTS OF GUI----------------*/
    VBox pane = new VBox();
    TextField messageBox = new TextField();
    Label TitleBox = new Label();
    ScrollPane scrollPane = new ScrollPane();
    VBox Users = new VBox(5);
    VBox chatpane = new VBox();
    Label message;
    Consumer<String> word;
    Consumer<Integer> Timer;
    BooleanProperty bp;

    private Consumer<Integer> onRecieveCallBack = aVoid -> Platform.runLater(() -> {
        socketClientchat.send("<stop>");
        messageBox.setDisable(false);
    });

    /*-------------- Code :) ----------------*/
    public void InitChat(String name, String Ip, int port){
        socketClientchat = new ClientChat(port, Ip, name, data -> {
            Platform.runLater(() -> {
                if(data.getClass().equals(ArrayList.class))
                {
                    Users.getChildren().clear();
                    ((ArrayList) data).forEach( o -> newUser((Client) o) );
                }
                else if(data.getClass().equals(ControlData.class))
                {
                    word.accept(((ControlData)data).mot);
                    messageBox.setDisable(true);
                    new Thread(() -> {
                        Timer();
                        onRecieveCallBack.accept(1);
                    }).start();
                }
                else if(data.getClass().equals(WordData.class))
                {
                    new Thread(this::Timer).start();
                    if (!((WordData)data).last.equals("null")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("The Last word is " + ((WordData)data).last);
                        alert.showAndWait();
                    }
                    word.accept(((WordData)data).start);
                    bp.setValue(true);
                }
                else
                {
                    Map<String, String> nameMssg = ClientChat.parseMessage((String) data);
                    message = new Label(nameMssg.get("name") + ": " + nameMssg.get("msg"));
                    chatpane.getChildren().add(message);
                }
            });
        });
        socketClientchat.startConnection();
        TitleBox.setText(name);
        TitleBox.setMinSize(250, 40);
        TitleBox.setAlignment(Pos.CENTER);
        TitleBox.getStyleClass().add("NameBox");
    }

    private void Timer() {
        try { Thread.sleep(10*1000); } catch (InterruptedException e) {}
        for (int i = 0; i < 60; i++) {
            try {
                Thread.sleep(1000);
                Timer.accept(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void initGui(){
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(chatpane);
        chatpane.setSpacing(5);
        chatpane.heightProperty().addListener((ChangeListener) (observable, oldvalue, newValue) -> scrollPane.setVvalue((Double)newValue ));
        messageBox.setPromptText("Kteb chno ban lik :) ");
        messageBox.getStyleClass().add("message-box");
        messageBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER) && !messageBox.getText().equals(""))
            {
                socketClientchat.send("<msg>"+messageBox.getText()+"<msg>");
                message = new Label();
                message.setText("Me: "+messageBox.getText());
                chatpane.getChildren().add(message);
                messageBox.setText("");
            }
        });
    }

    public VBox getUsers() {
        return Users;
    }

    public void newUser(Client client){
        Label n = new Label(client.name+"    "+client.score);
        n.setMinSize(200, 40);
        n.setMaxSize(200, 40);
        n.getStyleClass().add("userListItem");
        n.setAlignment(Pos.CENTER);
        Users.getChildren().add(n);
    }

    public void start(Consumer<String> word, Consumer<Integer> time, BooleanProperty bp) {
        this.word = word;
        this.bp = bp;
        this.Timer = time;
        chatpane.minWidthProperty().bind(scrollPane.widthProperty().subtract(3));
        chatpane.minHeightProperty().bind(scrollPane.heightProperty().subtract(3));
        scrollPane.minHeightProperty().bind(pane.heightProperty().subtract(80));
        scrollPane.maxHeightProperty().bind(pane.heightProperty().subtract(80));
        Users.minHeightProperty().bind(pane.heightProperty());
        Users.maxHeightProperty().bind(pane.heightProperty());
        messageBox.setMinHeight(40);
        messageBox.setMinWidth(235);
        messageBox.setMaxWidth(220);
        pane.getChildren().add(TitleBox);
        pane.getChildren().add(scrollPane);
        pane.getChildren().add(messageBox);
        chatpane.getStyleClass().add("chatpane");
        scrollPane.getStyleClass().add("chatpane");
        Users.getStyleClass().add("usersList");
        initGui();
    }

    public VBox getPane() { return pane; }
}
