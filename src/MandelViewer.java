import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.print.PrinterJob;
import javafx.embed.swing.SwingFXUtils;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MandelViewer extends Application{
	public static final boolean onMac = System.getProperty("os.name").startsWith("Mac");
	
	public static ColorType colortype = ColorType.SUNSET;
	public static int quality = 2;
	public static boolean minimal = true;
	public static boolean blur = true;

	public static double deltax = 0., deltay = 0.;
	
	MandelSet mandel;
	Label info;
	ProgressBar progressbar;
	
	public static void main(String[] args){
		Application.launch(args);
	}
	
	@Override
	public void start(Stage stage){
		stage.setTitle("MandelViewer");
		stage.setWidth(600);
		stage.setHeight(600);
		stage.setMinWidth(400);
		stage.setMinHeight(400);
		
		AnchorPane root = new AnchorPane();
		
		MenuBar menubar = new MenuBar();
		menubar.setUseSystemMenuBar(true);
		menubar.setPrefHeight(40);
		AnchorPane.setLeftAnchor(menubar, 0.);
		AnchorPane.setTopAnchor(menubar, 0.);
		
		Menu file = new Menu("ファイル");
		MenuItem restart = new MenuItem("新規");
		MenuItem save = new MenuItem("保存");
		MenuItem print = new MenuItem("印刷");
		MenuItem exit = new MenuItem("終了");
		setshortcut(restart, KeyCode.N);
		setshortcut(save, KeyCode.S);
		setshortcut(print, KeyCode.P);
		setshortcut(exit, KeyCode.Q);
		if(onMac){
			file.getItems().addAll(restart, save, print);
		}else{
			file.getItems().addAll(restart, save, print, exit);
		}
		
		Menu view = new Menu("表示");
		MenuItem zoomin = new MenuItem("拡大");
		MenuItem zoomout = new MenuItem("縮小");
		MenuItem culcmore = new MenuItem("計算量を増やす");
		MenuItem culcless = new MenuItem("計算量を減らす");
		CheckMenuItem fullscreen = new CheckMenuItem("全画面表示");
		setshortcut(zoomin, KeyCode.UP);
		setshortcut(zoomout, KeyCode.DOWN);
		setshortcut(culcmore, KeyCode.PLUS);
		setshortcut(culcless, KeyCode.MINUS);
		setshortcut(fullscreen, KeyCode.F);
		stage.setFullScreenExitHint("");
		
		Menu setcolor = new Menu("色");
		ToggleGroup colorgroup = new ToggleGroup();
		RadioMenuItem sunset = new RadioMenuItem("夕焼け");
		RadioMenuItem rainbow = new RadioMenuItem("虹");
		RadioMenuItem leaf = new RadioMenuItem("葉");
		RadioMenuItem crystal = new RadioMenuItem("水晶");
		RadioMenuItem twilight = new RadioMenuItem("黄昏");
		sunset.setToggleGroup(colorgroup);
		rainbow.setToggleGroup(colorgroup);
		leaf.setToggleGroup(colorgroup);
		crystal.setToggleGroup(colorgroup);
		twilight.setToggleGroup(colorgroup);
		sunset.setSelected(true);
		setcolor.getItems().addAll(sunset, rainbow, leaf, crystal, twilight);
		
		Menu setquality = new Menu("画質");
		ToggleGroup qualitygroup = new ToggleGroup();
		RadioMenuItem high = new RadioMenuItem("高");
		RadioMenuItem medium = new RadioMenuItem("中");
		RadioMenuItem low = new RadioMenuItem("低");
		high.setToggleGroup(qualitygroup);
		medium.setToggleGroup(qualitygroup);
		low.setToggleGroup(qualitygroup);
		medium.setSelected(true);
		setquality.getItems().addAll(high, medium, low);
		
		CheckMenuItem setminimal = new CheckMenuItem("描画を最小限にする");		
		CheckMenuItem setblur = new CheckMenuItem("描画を滑らかにする");
		setminimal.setSelected(true);
		setblur.setSelected(true);
		GaussianBlur gblur = new GaussianBlur(1+quality);
		
		view.getItems().addAll(zoomin, zoomout, culcmore, culcless, fullscreen,
				new SeparatorMenuItem(), setcolor, setquality, setminimal, setblur);
		menubar.getMenus().addAll(file, view);
		
		FileChooser filechooser = new FileChooser();
		filechooser.setInitialFileName("*.png");
		
		restart.setOnAction(event -> start(stage));
		save.setOnAction(event -> {
			File savefile = filechooser.showSaveDialog(stage);
			mandel.save(savefile);
		});
		print.setOnAction(event -> mandel.print(stage));
		exit.setOnAction(event -> Platform.exit());
		
		zoomin.setOnAction(event -> mandel.zoom(1/1.2));
		zoomout.setOnAction(event -> mandel.zoom(1.2));
		culcmore.setOnAction(event -> {
			mandel.draw(1., 1.1);			
		});
		culcless.setOnAction(event -> {
			mandel.draw(1., 1/1.1);
		});
		fullscreen.setOnAction(event -> {
			if(fullscreen.isSelected()){
				stage.setFullScreen(true);
				info.setText(" escキーで全画面表示を終了できます。");
			}else{
				stage.setFullScreen(false);
			}
		});
		stage.fullScreenProperty().addListener((observable, oldValue, isFullscreen) -> 
			fullscreen.setSelected(isFullscreen));
		
		sunset.setOnAction(event -> {
			colortype = ColorType.SUNSET;
			mandel.draw();
		});
		rainbow.setOnAction(event -> {
			colortype = ColorType.RAINBOW;
			mandel.draw();
		});
		leaf.setOnAction(event -> {
			colortype = ColorType.LEAF;
			mandel.draw();
		});
		crystal.setOnAction(event -> {
			colortype = ColorType.CRYSTAL;
			mandel.draw();
		});
		twilight.setOnAction(event -> {
			colortype = ColorType.TWILIGHT;
			mandel.draw();
		});
		
		high.setOnAction(event -> {
			quality = 1;
			gblur.setRadius(1+quality);
			mandel.draw();
		});
		medium.setOnAction(event -> {
			quality = 2;
			gblur.setRadius(1+quality);
			mandel.draw();
		});
		low.setOnAction(event -> {
			quality = 3;
			gblur.setRadius(1+quality);
			mandel.draw();
		});
		
		setminimal.setOnAction(event -> {
			minimal = setminimal.isSelected();
			if(minimal){
				mandel.setWidth(0.5*mandel.getWidth());
				mandel.setHeight(0.5*mandel.getHeight());
				AnchorPane.setLeftAnchor(mandel, 0.);
				AnchorPane.setTopAnchor(mandel, 0.);
				mandel.draw(0.5, 1.);
			}else{
				mandel.setWidth(2*mandel.getWidth());
				mandel.setHeight(2*mandel.getHeight());
				AnchorPane.setLeftAnchor(mandel, -mandel.getWidth()/4);
				AnchorPane.setTopAnchor(mandel, -mandel.getWidth()/4);
				mandel.draw(2., 1.);
			}
		});
		setblur.setOnAction(event -> {
			blur = setblur.isSelected();
			if(blur){
				mandel.setEffect(gblur);
			}else{
				mandel.setEffect(null);
			}
		});
				
		BorderPane statusbar = new BorderPane();
		statusbar.setPrefWidth(600);
		statusbar.setPrefHeight(40);
	    statusbar.setStyle("-fx-background-color: #E0E0E0;");
		AnchorPane.setLeftAnchor(statusbar, 0.);
		AnchorPane.setTopAnchor(statusbar, 560.);
		info = new Label("");
		progressbar = new ProgressBar();
		progressbar.setVisible(false);
		statusbar.setLeft(info);
		statusbar.setRight(progressbar);
		
		AnchorPane board = new AnchorPane();
		AnchorPane.setLeftAnchor(board, 0.);
		AnchorPane.setTopAnchor(board, onMac? 0: menubar.getHeight());
		
		if(minimal){
			mandel = new MandelSet(600, 600, -2.1, 0.5, -1.3, 1.3, 300);
		}else{
			mandel = new MandelSet(1200, 1200, -3.4, 1.8, -2.6, 2.6, 300);	
		}
		mandel.draw();
		AnchorPane.setLeftAnchor(mandel, minimal? 0.: -300.);
		AnchorPane.setTopAnchor(mandel, minimal? 0.: -300.);
		if(blur) mandel.setEffect(gblur);
		
		Slider zoomslider = new Slider(-1., 1., 0.);
		zoomslider.setOrientation(Orientation.VERTICAL);
		zoomslider.setPrefHeight(180);
		AnchorPane.setLeftAnchor(zoomslider, 30.);
		AnchorPane.setTopAnchor(zoomslider, 120.);
		zoomslider.setBlockIncrement(0.);
		
		Label zoominlabel = new Label("+");
		Label zoomoutlabel = new Label("-");
		zoominlabel.setPrefWidth(20);
		zoomoutlabel.setPrefWidth(20);
		VBox labels = new VBox();
		labels.setSpacing(145);
		AnchorPane.setLeftAnchor(labels, 50.);
		AnchorPane.setTopAnchor(labels, 120.);		
		labels.getChildren().addAll(zoominlabel, zoomoutlabel);
		
		Button culcmorebutton = new Button("+");
		Button culclessbutton = new Button("-");
		culcmorebutton.setPrefWidth(30);
		culclessbutton.setPrefWidth(30);
		VBox buttons = new VBox();
		AnchorPane.setLeftAnchor(buttons, 30.);
		AnchorPane.setTopAnchor(buttons, 50.);
		buttons.getChildren().addAll(culcmorebutton, culclessbutton);
		
		board.getChildren().addAll(mandel, zoomslider, labels, buttons);
		
		root.getChildren().addAll(menubar, board, statusbar);
		
		stage.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
			statusbar.setPrefWidth(newSceneWidth.doubleValue());
			mandel.setWidth((minimal? 1: 2)*newSceneWidth.doubleValue());
			AnchorPane.setLeftAnchor(mandel, minimal? 0: -mandel.getWidth()/4);
			mandel.draw();
		});
		stage.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> {
			mandel.setHeight((minimal? 1: 2)*newSceneHeight.doubleValue());
			AnchorPane.setTopAnchor(mandel, minimal? 0: -mandel.getHeight()/4);
			AnchorPane.setTopAnchor(statusbar, newSceneHeight.doubleValue()-statusbar.getHeight());
			mandel.draw();
		});
		
		mandel.setOnMouseMoved(event -> {
			final double ratio = (mandel.getXmax()-mandel.getXmin())/mandel.getWidth();
			final double x = mandel.getXmin()+ratio*(event.getSceneX()+(minimal? 0: mandel.getWidth()/4));
			final double y = -mandel.getYmin()-ratio*(event.getSceneY()+(minimal? 0: mandel.getHeight()/4));
			info.setText(" ("+x+", "+y+")");
		});
		mandel.setOnMouseExited(event -> info.setText(""));
		mandel.setOnMousePressed(event -> {
			deltax = event.getSceneX();
			deltay = event.getSceneY();
		});
		mandel.setOnMouseDragged(event -> {
			AnchorPane.setLeftAnchor(mandel, event.getSceneX()-deltax-(minimal? 0: mandel.getWidth()/4));
			AnchorPane.setTopAnchor(mandel, event.getSceneY()-deltay-(minimal? 0: mandel.getHeight()/4));
		});
		mandel.setOnMouseReleased(event -> {
			deltax -= event.getSceneX();
			deltay -= event.getSceneY();
			mandel.translate();
		});
		mandel.setOnScroll(event -> mandel.zoom(Math.pow(0.998, event.getDeltaY())));
		
		zoomslider.setOnMousePressed(event -> mandel.zoom(Math.pow(0.5, zoomslider.getValue())));
		zoomslider.setOnMouseDragged(event -> mandel.zoom(Math.pow(0.5, zoomslider.getValue())));
		zoomslider.setOnMouseReleased(event -> zoomslider.setValue(0.));
		zoomslider.setOnKeyPressed(event -> handlekey(event));
		
		culcmorebutton.setOnAction(event -> {
			mandel.draw(1., 1.1);
		});
		culclessbutton.setOnAction(event -> {
			mandel.draw(1., 1/1.1);
		});
		
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("UNEXPECTED ERROR");
            alert.setHeaderText("エラー: "+throwable.toString());
            alert.setContentText("予期せぬエラーが発生したため、プログラムを再起動します。");
            alert.showAndWait().ifPresent(response -> {
            	if(response == ButtonType.OK) start(stage);
            });
        });
		
		stage.setScene(new Scene(root));
		stage.show();
	}
	
	public void setshortcut(MenuItem menuitem, KeyCode keycode){
		if(onMac){
			menuitem.setAccelerator(new KeyCodeCombination(keycode, KeyCombination.META_DOWN));
		}else{
			menuitem.setAccelerator(new KeyCodeCombination(keycode, KeyCombination.CONTROL_DOWN));			
		}
	}
	
	public String getformat(File savefile){
		final String filename = savefile.toURI().toString();
		final String[] formats = ImageIO.getWriterFormatNames();
		for(int n = 0; n < formats.length; n++){
			if(filename.endsWith(formats[n])) return formats[n];
		}
		return "png";
	}
	
	public void handlekey(KeyEvent event){
		if(event.isControlDown() || event.isMetaDown()) return;
		switch(event.getCode()){
			case RIGHT:
				deltax = 40.;
				break;
			case LEFT:
				deltax = -40.;
				break;
			case UP:
				deltay = -40.;
				break;
			case DOWN:
				deltay = 40.;
				break;
			default:
				return;
		}
		mandel.translate();
	}
	
	public enum ColorType{
		SUNSET{
			public void color(GraphicsContext gc, double parameter){
				if(parameter >= 1.){
					r = 0;
					g = 0;
					b = 0;
				}else{
					h = parameter;
					s = 0.6;
					v = 1.;
					convert();
				}
				gc.setFill(Color.rgb(r, g, b));
			}			
		},
		RAINBOW{
			public void color(GraphicsContext gc, double parameter){
				if(parameter >= 1.){
					r = 0;
					g = 0;
					b = 0;
				}else{
					h = 6*parameter-Math.floor(6*parameter);
					s = 0.5;
					v = 0.9+0.1*Math.cos(24*Math.PI*parameter);
					convert();
				}
				gc.setFill(Color.rgb(r, g, b));
			}
		},
		LEAF{
			public void color(GraphicsContext gc, double parameter){
				if(parameter >= 1.){
					r = 0;
					g = 0;
					b = 0;
				}else{
					h = 0.3;
					s = 0.3+0.3*Math.sin(12*Math.PI*parameter);
					v = 0.7+0.3*Math.cos(16*Math.PI*parameter);
					convert();
				}
				gc.setFill(Color.rgb(r, g, b));
			}
		},
		CRYSTAL{
			public void color(GraphicsContext gc, double parameter){
				if(parameter >= 1.){
					r = 0;
					g = 0;
					b = 0;
				}else{
					h = 0.55;
					s = 0.3+0.3*Math.sin(12*Math.PI*parameter);
					v = 0.7+0.3*Math.cos(16*Math.PI*parameter);
					convert();
				}
				gc.setFill(Color.rgb(r, g, b));
			}
		},
		TWILIGHT{
			public void color(GraphicsContext gc, double parameter){
				r = (int)(parameter*256*7)%256;
				g = (int)(parameter*256*5)%256;
				b = (int)(parameter*256*3)%256;
				gc.setFill(Color.rgb(r, g, b));
			}
		};
		abstract void color(GraphicsContext gc, double parameter);
		
		private static int r, g, b;
		private static double h, s, v;
		
		private static void convert(){
			r = (int)(255*v);
			g = (int)(255*v);
			b = (int)(255*v);
			if(s <= 0.) return;
		    h *= 6.;
		    final int i = (int)h;
		    final double f = h-(double)i;
		    switch(i){
		        case 0:
		            g *= 1-s*(1-f);
		            b *= 1-s;
		            break;
		        case 1:
		            r *= 1-s*f;
		            b *= 1-s;
		            break;
		        case 2:
		            r *= 1-s;
		            b *= 1-s*(1-f);
		            break;
		        case 3:
		            r *= 1-s;
		            g *= 1-s*f;
		            break;
		        case 4:
		            r *= 1-s*(1-f);
		            g *= 1-s;
		            break;
		        case 5:
		            g *= 1-s;
		            b *= 1-s*f;
		            break;
		    }
		}
	}
	
	public class MandelSet extends Canvas{
		private double xmin, xmax, ymin, ymax;
		private int nmax;
		private GraphicsContext gc;
		
		MandelSet(double width, double height, double xmin, double xmax, double ymin, double ymax, int nmax){
			super(width, height);
			this.xmin = xmin;
			this.xmax = xmax;
			this.ymin = ymin;
			this.ymax = ymax;
			this.nmax = nmax;
			gc = getGraphicsContext2D();
		}
		
		public void save(File savefile){
			if(savefile == null) return;
			WritableImage image = mandel.snapshot(new SnapshotParameters(), null);
			try{
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), getformat(savefile), savefile);
			}catch(IOException error){
	        	Alert alert = new Alert(Alert.AlertType.ERROR);
	        	alert.setTitle("ERROR WRITING TO FILE");
	        	alert.setHeaderText("書き込みエラー");
	        	alert.setContentText("ファイルの書き込みに失敗しました。やり直してください。");
	        	alert.showAndWait();
			}
		}
		
		public void print(Stage stage){
			PrinterJob printerjob = PrinterJob.createPrinterJob();
			if(printerjob == null){
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("NO PRINTERS AVAILABLE");
	            alert.setHeaderText("プリンタエラー");
	            alert.setContentText("使用可能なプリンタがありません。");
	            alert.showAndWait();
	            return;
			}
			printerjob.showPrintDialog(stage);
			printerjob.printPage(mandel);
			printerjob.endJob();
		}
				
		public void translate(){
			draw();
			AnchorPane.setLeftAnchor(mandel, minimal? 0: -getWidth()/4);
			AnchorPane.setTopAnchor(mandel, minimal? 0: -getHeight()/4);
			deltax = 0.;
			deltay = 0.;
		}
		
		public void zoom(double scale){
			setScaleX(scale);
			setScaleY(scale);
			draw(scale, 1/Math.pow(scale,0.2));
			setScaleX(1.);
			setScaleY(1.);
		}
		
		public void draw(double scale, double culc){
			final int intwidth = (int)getWidth();
			final int intheight = (int)getHeight();
			final double ratio = xmax-xmin < ymax-ymin? (xmax-xmin)/intwidth: (ymax-ymin)/intheight;
			final double newxmin = (xmax+xmin)/2-scale*(xmax-xmin)/2+ratio*deltax;
			final double newxmax = (xmax+xmin)/2+scale*(xmax-xmin)/2+ratio*deltax;
			final double newymin = (ymax+ymin)/2-scale*(ymax-ymin)/2+ratio*deltay;
			final double newymax = (ymax+ymin)/2+scale*(ymax-ymin)/2+ratio*deltay;
			final int newnmax = (int)(nmax*culc);
			this.xmin = newxmin;
			this.xmax = newxmax;
			this.ymin = newymin;
			this.ymax = newymax;
			this.nmax = newnmax;
			final double distance = Math.min(intwidth, intheight);
			Task<Void> task = new Task<Void>(){
				@Override
				protected Void call(){
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							int i, j;
							double c, d;
							for (i = 0; i < intwidth; i += quality){
								for (j = 0; j < intheight; j +=	quality){
									c = (xmin+xmax)/2+(xmax-xmin)*(i-intwidth/2)/distance;
									d = (ymin+ymax)/2+(ymax-ymin)*(j-intheight/2)/distance;
									colortype.color(gc, mandel(c, d));
									gc.fillRect(i, j, quality, quality);
								}
								updateProgress(i, intwidth-1);
							}
						}
					});
					return null;
				}
			};
			final ExecutorService exe = Executors.newSingleThreadExecutor();
			info.setText(" 描画中...");
			progressbar.setVisible(true);
			progressbar.progressProperty().bind(task.progressProperty());
			exe.submit(task);
			task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>(){
				@Override
				public void handle(WorkerStateEvent event){
					exe.shutdown();
					info.setText("");
					progressbar.setVisible(false);					
				}
			});
		}
		public void draw(){
			draw(1., 1.);
		}
		
		public double mandel(double c, double d){
			double x = 0., y = 0.;
			double u, v;
			int n;
			for (n = 0; n < nmax; n++){
				u = x*x-y*y+c;
				v = 2*x*y+d;
				if (u*u+v*v > 4.) break;
				x = u;
				y = v;
			}
			return (double)n/nmax;
		}
		
		public double getXmin() {
			return xmin;
		}
		public double getXmax() {
			return xmax;
		}
		public double getYmin() {
			return ymin;
		}
		public double getYmax() {
			return ymax;
		}
		public int getNmax() {
			return nmax;
		}
	}
}

