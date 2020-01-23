import java.awt.image.RenderedImage;
import java.io.*;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Window {
    /* -------------------- Canvas ---------------------- */
    BorderPane pane = new BorderPane();
	
	/* ----------btns---------- */
    ToggleButton drowbtn = new ToggleButton("Draw");
    ToggleButton rubberbtn = new ToggleButton("Rubber");
	Button save = new Button("Save");
    Button open = new Button("Open");

    HBox btns = new HBox(10);

    ColorPicker cpLine = new ColorPicker(Color.BLACK);

    Slider slider = new Slider(1, 50, 3);
    
    /* ----------Drow Canvas---------- */
    Canvas canvas = new Canvas();
    GraphicsContext gc;

    /* -------------- Sockets start----------------- */
    ClientObject clientObject;

    public void InitWin(String Ip, int port){
        clientObject = new ClientObject(port, Ip, data -> {
            Platform.runLater(() -> {
                if (data.type.equals("drow")) {
                    if (data.state.equals("beginPath")) gc.beginPath();
                    else if (data.state.equals("stroke")) gc.stroke();
                    else if (data.state.equals("closePath")) gc.closePath();
                    gc.setStroke(Paint.valueOf(data.color));
                    gc.setLineWidth(data.width);
                    gc.lineTo(data.x, data.y);
                }else{
                    gc.clearRect(data.x - data.width / 2, data.y - data.width / 2, data.width, data.width);
                }
            });
        });
    }

    private void sendCanvas(double x, double y, String state) {
        Data data = new Data(gc.getStroke().toString(), (drowbtn.isSelected())?"drow":"rubber", state, gc.getLineWidth(),x,y);
        clientObject.send(data);
    }
    /* -------------- Sockets end ----------------- */

    public void initComponents(){
        ToggleButton[] toolsArr = {drowbtn, rubberbtn};
        ToggleGroup tools = new ToggleGroup();
        for (ToggleButton tool : toolsArr) {
            tool.setMinWidth(90);
            tool.getStyleClass().add("btn");
        }

        Button[] basicArr = {save, open};
        for(Button btn : basicArr) {
            btn.setMinWidth(90);
            btn.getStyleClass().add("btn");
        }

        btns.getChildren().addAll(drowbtn, rubberbtn, cpLine, slider, open, save);
        btns.setPadding(new Insets(5));

        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1);
   
    }
	
	public void run(){
        rubberbtn.selectedProperty().addListener((observableValue, aBoolean, t1) -> drowbtn.setSelected(aBoolean));
        drowbtn.selectedProperty().addListener((observableValue, aBoolean, t1) -> rubberbtn.setSelected(aBoolean));
        canvas.setOnMousePressed(e->{
           if(drowbtn.isSelected()) {
               gc.setStroke(cpLine.getValue());
               gc.beginPath();
               gc.lineTo(e.getX(), e.getY());
           }
           else if(rubberbtn.isSelected()) {
               double lineWidth = gc.getLineWidth();
               gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
           }
           sendCanvas(e.getX(), e.getY(), "beginPath");
        });
       
        canvas.setOnMouseDragged(e->{
           if(drowbtn.isSelected()) {
               gc.lineTo(e.getX(), e.getY());
               gc.stroke();
           }
           else if(rubberbtn.isSelected()){
               double lineWidth = gc.getLineWidth();
               gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
           }
           sendCanvas(e.getX(), e.getY(), "stroke");
        });
       
        canvas.setOnMouseReleased(e->{
            if(drowbtn.isSelected()) {
               gc.lineTo(e.getX(), e.getY());
               gc.stroke();
               gc.closePath();
            }
            else if(rubberbtn.isSelected()) {
               double lineWidth = gc.getLineWidth();
               gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
            sendCanvas(e.getX(), e.getY(), "closePath");
        });
        // color picker
        cpLine.setOnAction(e->gc.setStroke(cpLine.getValue()));

        // slider
        slider.valueProperty().addListener(e->{
            double width = slider.getValue();
            gc.setLineWidth(width);
        });
	}

    public void OpenSave(Stage primaryStage){
       /*------- Save & Open ------*/
       // Open
       open.setOnAction((e)->{
           FileChooser openFile = new FileChooser();
           openFile.setTitle("Open File");
           File file = openFile.showOpenDialog(primaryStage);
           if (file != null) {
               try {
                   InputStream io = new FileInputStream(file);
                   Image img = new Image(io);
                   gc.drawImage(img, 0, 0);
               } catch (IOException ex) {
                   System.out.println("Error!");
               }
           }
       });
       
       // Save
       save.setOnAction((e)->{
           FileChooser savefile = new FileChooser();
           savefile.setTitle("Save File");
           
           File file = savefile.showSaveDialog(primaryStage);
           if (file != null) {
               try {
                   WritableImage writableImage = new WritableImage(1080, 790);
                   canvas.snapshot(null, writableImage);
                   RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                   ImageIO.write(renderedImage, "png", file);
               } catch (IOException ex) {
                   System.out.println("Error!");
               }
           }
       });
	}

	public BooleanProperty start(Stage stage){
		initComponents();
		run();
		OpenSave(stage);
		btns.setMinHeight(33);
		btns.setMaxHeight(33);
        btns.setAlignment(Pos.CENTER);
        btns.getStyleClass().add("bar");
		pane.setCenter(canvas);
		pane.setBottom(btns);
		canvas.heightProperty().bind(pane.heightProperty().subtract(70));
        canvas.widthProperty().bind(pane.widthProperty());
        return btns.disableProperty();
    }
	
	public BorderPane getPane(){
		return pane;
	}
	
}
