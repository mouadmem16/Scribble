import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class Main extends Application {
	BorderPane primaryPane = new BorderPane();
	Chatting chat = new Chatting();
	Window win = new Window();
	Label label = new Label();
	Label time = new Label();
	BooleanProperty bp;
	Consumer<String> word1 = s -> {
		Platform.runLater(() ->  {
			bp.setValue(false);
			label.setText(s);
			win.gc.clearRect(0,0, win.canvas.getWidth(), win.canvas.getHeight());
		});
	};
	Consumer<Integer> timer = i -> {
		Platform.runLater(() -> time.setText(String.valueOf(i)));
	};

	@Override
	public void start(Stage primaryStage) {
		bp = win.start(primaryStage);
		BorderPane windowpane = win.getPane();

		chat.start(word1, timer, bp);
		VBox chatpane = chat.getPane();

		HBox p = new HBox();
		p.setSpacing(30);
		p.setMinHeight(30);
		p.setMinHeight(30);
		p.getStyleClass().add("wordBox");
		label.getStyleClass().add("label1");
		p.setAlignment(Pos.CENTER);
		time.setAlignment(Pos.CENTER);
		p.getChildren().add(time);
		p.getChildren().add(label);
		p.maxWidthProperty().bind(primaryPane.widthProperty());
		p.minWidthProperty().bind(primaryPane.widthProperty());

		time.getStyleClass().add("time");
		primaryPane.setTop(p);

		primaryPane.setLeft(chat.getUsers());
		primaryPane.setCenter(windowpane);
		windowpane.maxWidthProperty().bind(primaryPane.widthProperty().subtract(450));
		windowpane.maxHeightProperty().bind(primaryPane.heightProperty());
		primaryPane.setRight(chatpane);
		getConnectionBox();
		primaryStage.setResizable(false);
		Scene scene = new Scene(primaryPane, 1100, 600);
		scene.getStylesheets().add(getClass().getResource("Mycss.css").toExternalForm());
		primaryStage.setTitle("Sricbble");
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest(windowEvent -> {
			chat.socketClientchat.closeConnection();
		});
   }

   public void getConnectionBox(){
		Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.CLOSE);
		VBox pane = new VBox();
		TextField conTxt = new TextField(), Iptxt = new TextField();
		alert.setOnCloseRequest((event) -> {
			String name, Ip;
			name = (conTxt.getText().equals(""))? "Geust": conTxt.getText();
			Ip = (Iptxt.getText().equals(""))? "localhost" : Iptxt.getText();
			chat.InitChat(name, Ip, 9999); win.InitWin(Ip, 9998);
		});
		conTxt.setPromptText("Your name, By default Geust");
		Iptxt.setPromptText("the Server, By default localhost");
		conTxt.setTranslateY(106); conTxt.setTranslateX(-5);
		Iptxt.setTranslateY(108); Iptxt.setTranslateX(-5);

		alert.setTitle("Connexion Scribll"); alert.setHeaderText(null);
		pane.setMinSize(300,400); pane.setMaxSize(300,400);
		Iptxt.getStyleClass().add("labelInAlert"); conTxt.getStyleClass().add("labelInAlert");
		conTxt.setPrefSize(170, 50); Iptxt.setPrefSize(170, 50);
		pane.getChildren().add(conTxt); pane.getChildren().add(Iptxt);

		alert.setResizable(false);
		alert.getDialogPane().getStyleClass().add("alert");
		alert.getDialogPane().getScene().getStylesheets().add(getClass().getResource("Mycss.css").toExternalForm());
		alert.getDialogPane().setContent(pane);
		alert.showAndWait();
   }



   public static void main(String[] args) {
       launch(args);
   }
}