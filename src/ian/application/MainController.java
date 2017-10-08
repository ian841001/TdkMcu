package ian.application;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import ian.main.capture.struct.ExtraPic;
import ian.main.surveillance.Cmd;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class MainController implements Initializable {
	
	@FXML Button btnConn, btnDisc;
	@FXML TextField txtIp, txtPort;
	@FXML Label lblStatus;
	
	
	@FXML Rectangle motorRec1, motorRec2, motorRec3, motorRec4;
	@FXML Label motorLbl1, motorLbl2, motorLbl3, motorLbl4;
	Rectangle[] motorRec;
	Label[] motorLbl;
	
	@FXML Rectangle rcRec1, rcRec2, rcRec3, rcRec4, rcRec5, rcRec6, rcRec7, rcRec8;
	@FXML Label rcLbl1, rcLbl2, rcLbl3, rcLbl4, rcLbl5, rcLbl6, rcLbl7, rcLbl8;
	Rectangle[] rcRec;
	Label[] rcLbl;
	
	@FXML Label altLbl;
	
	@FXML Label modeLbl1, modeLbl2, modeLbl3, modeLbl4;
	
	@FXML Label rpiLbl1, rpiLbl2, rpiLbl3, rpiLbl4;
	
	@FXML Label mwcLblDebug0, mwcLblDebug1, mwcLblDebug2, mwcLblDebug3;
	Label[] mwcLblDebug;
	
	@FXML Label rpiLblDebug0, rpiLblDebug1, rpiLblDebug2, rpiLblDebug3, rpiLblDebug4, rpiLblDebug5, rpiLblDebug6, rpiLblDebug7;
	Label[] rpiLblDebug;
	
	@FXML TextField extraInfoTxt0, extraInfoTxt1, extraInfoTxt2, extraInfoTxt3, extraInfoTxt4, extraInfoTxt5, extraInfoTxt6, extraInfoTxt7;
	@FXML Label extraInfoLbl0, extraInfoLbl1, extraInfoLbl2, extraInfoLbl3, extraInfoLbl4, extraInfoLbl5, extraInfoLbl6, extraInfoLbl7;
	TextField[] extraInfoTxt;
	Label[] extraInfoLbl;
	
	@FXML Label msgLbl;
	
	@FXML ImageView imageView0, imageView1;
	ImageView[] imageView;
	
	@FXML Label capLbl0, capLbl1, capLbl2, capLbl3;
	Label[] capLbl;
	
	@FXML Label extraMsgLbl0;
	
	@FXML Label mcuModeLbl0, mcuModeLbl1, mcuModeLbl2, mcuModeLbl3;
	
	
	private BufferedImage bufferedImage = new BufferedImage(500,  500, BufferedImage.TYPE_INT_ARGB);
	private Graphics2D g = bufferedImage.createGraphics();
	private Image[] image;
	
	private Repeater repeater;
	
	private int cycleTime;
	private int step, setWantAlt;
	
	private byte level;
	private int[] debug = new int[8];
	private byte[] extraInfo = new byte[8];
	
	
	private String msgStr = "message from rpi";
	private String extraMsgStr = "message from rpi";
	
	
	
	
	private void updateLblStatus(int mode) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lblStatus.setText(new String[]{"Disconnected.","Connecting...","Connected."}[mode]);
				lblStatus.setStyle("-fx-background-color: " + new String[]{"#FF8080;","#FFFF00;","#66FF66;"}[mode]);
			}
		});
	}
	
	private void updateGui() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < motorRec.length; i++) {
					motorRec[i].setWidth((Main.info.motor[i] - 1000) / 2);
					motorLbl[i].setText(String.valueOf(Main.info.motor[i]));
				}
				
				for (int i = 0; i < rcRec.length; i++) {
					rcRec[i].setWidth((Main.info.rc[i] - 1000) / 10);
					rcLbl[i].setText(String.valueOf(Main.info.rc[i]));
				}
				
				altLbl.setText(String.valueOf(Main.info.altEstAlt));
				
				modeLbl1.setStyle("-fx-background-color: " + (Main.info.ok_to_arm  ? "green" : "red"));
				modeLbl2.setStyle("-fx-background-color: " + (Main.info.angle_mode ? "green" : "red"));
				modeLbl3.setStyle("-fx-background-color: " + (Main.info.armed      ? "green" : "red"));
				modeLbl4.setStyle("-fx-background-color: " + (Main.info.baro_mode  ? "green" : "red"));
				altLbl.setStyle("-fx-background-color: " + (Main.info.isSonarOk  ? "green" : "red"));
				
				mcuModeLbl0.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[Main.info.armMode ]));
				mcuModeLbl1.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[Main.info.baroMode]));
				mcuModeLbl2.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[Main.info.yawMode ]));
				mcuModeLbl3.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[Main.info.rpMode  ]));
				
				
				
				rpiLbl1.setText(String.valueOf(step));
				rpiLbl2.setText(String.valueOf(setWantAlt));
				rpiLbl3.setText(String.valueOf(Main.info.altHold));
				rpiLbl4.setText(String.valueOf(cycleTime));
				
				
				for (int i = 0; i < mwcLblDebug.length; i++) {
					mwcLblDebug[i].setText(String.valueOf(Main.info.debug[i]));
				}
				
				for (int i = 0; i < rpiLblDebug.length; i++) {
					rpiLblDebug[i].setText(String.valueOf(debug[i]));
				}
				
				
				msgLbl.setStyle("-fx-background-color: " + (new String[]{"#66FF66", "#FFFF00", "#FF8080"}[level]));
				msgLbl.setText(msgStr);
				
				extraMsgLbl0.setText(extraMsgStr);
				
				for (int i = 0; i < extraInfoLbl.length; i++) {
					extraInfoLbl[i].setText(String.valueOf(extraInfo[i]));
				}
				
				
				
				for (int i = 0; i < image.length; i++) {
					if (image[i] != null) {
						imageView[i].setImage(image[i]);
					}
				}
				
				
				capLbl0.setText(String.valueOf(Main.info.captureStatus));
				capLbl1.setText(String.valueOf(Main.info.captureDeltaX));
				capLbl2.setText(String.valueOf(Main.info.captureDeltaY));
				capLbl3.setText(String.valueOf(Main.info.captureAngle));
				
				
				
			}
		});
	}
	
	private void conn() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateLblStatus(1);
				try {
					Main.mcuSocket.conn(txtIp.getText(), Integer.valueOf(txtPort.getText()));
					updateLblStatus(2);
					repeater.start();
				} catch (IOException e) {
					e.printStackTrace();
					updateLblStatus(0);
				}
			}
		}).start();
	}
	private void disc() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					repeater.stop();
					Main.mcuSocket.disc();
				} catch (IOException e) {
					e.printStackTrace();
				}
				updateLblStatus(0);
			}
		}).start();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		motorRec = new Rectangle[]{motorRec1, motorRec2, motorRec3, motorRec4};
		motorLbl = new Label[]{motorLbl1, motorLbl2, motorLbl3, motorLbl4};
		rcRec = new Rectangle[]{rcRec1, rcRec2, rcRec3, rcRec4, rcRec5, rcRec6, rcRec7, rcRec8};
		rcLbl = new Label[]{rcLbl1, rcLbl2, rcLbl3, rcLbl4, rcLbl5, rcLbl6, rcLbl7, rcLbl8};
		mwcLblDebug = new Label[]{mwcLblDebug0, mwcLblDebug1, mwcLblDebug2, mwcLblDebug3};
		rpiLblDebug = new Label[]{rpiLblDebug0, rpiLblDebug1, rpiLblDebug2, rpiLblDebug3, rpiLblDebug4, rpiLblDebug5, rpiLblDebug6, rpiLblDebug7};
		extraInfoTxt = new TextField[]{extraInfoTxt0, extraInfoTxt1, extraInfoTxt2, extraInfoTxt3, extraInfoTxt4, extraInfoTxt5, extraInfoTxt6, extraInfoTxt7};
		extraInfoLbl = new Label[]{extraInfoLbl0, extraInfoLbl1, extraInfoLbl2, extraInfoLbl3, extraInfoLbl4, extraInfoLbl5, extraInfoLbl6, extraInfoLbl7};
		imageView = new ImageView[]{imageView0, imageView1};
		image = new Image[imageView.length];
		capLbl = new Label[]{capLbl0, capLbl1, capLbl2, capLbl3};
		
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		for (int i = 0; i < image.length; i++) {
			image[i] = SwingFXUtils.toFXImage(bufferedImage, null);
		}
		
		updateGui();
		
		Main.s = new ScheduledThreadPoolExecutor(1);
		btnConn.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				conn();
			}
		});
		btnDisc.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				disc();
			}
		});
		updateLblStatus(0);
		
		txtIp.setText(McuSocket.DEFAULT_IP);
		txtPort.setText(String.valueOf(McuSocket.DEFAULT_PORT));
		
		for (int i = 0; i < extraInfoTxt.length; i++) {
			extraInfoTxt[i].focusedProperty().addListener(new MyChangeListener(i) {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
			        if (!newPropertyValue) {
			        	try {
							extraInfo[index] = Byte.parseByte(extraInfoTxt[index].getText());
						} catch (NumberFormatException e) {
							extraInfo[index] = 0;
						}
			        }
			    }
			});
		}
		
		
		repeater = new Repeater(new Runnable() {
			@Override
			public void run() {
				try {
					Main.info.setAll(Main.mcuSocket.cmd(Cmd.CMD_GET_INFO));
					
					
					
					ByteBuffer buffer = ByteBuffer.wrap(Main.mcuSocket.cmd(Cmd.CMD_GET_RPI_INFO)).order(ByteOrder.LITTLE_ENDIAN);
					step = buffer.getInt();
					setWantAlt = buffer.getInt();
					cycleTime = buffer.getInt();
					level = buffer.get();
					for (int i = 0; i < debug.length; i++) {
						debug[i] = buffer.getInt();
					}
					
					
					
					
					msgStr = new String(Main.mcuSocket.cmd(Cmd.CMD_GET_MSG));
					extraMsgStr = new String(Main.mcuSocket.cmd(Cmd.CMD_GET_EXTRA_MSG));
					
					
					
					
					ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
					for (int i = 0; i < extraInfoTxt.length; i++) {
						byteBuffer.put(extraInfo[i]);
					}
					Main.mcuSocket.cmd(Cmd.CMD_SET_STATUS, byteBuffer.array());
					
					
					
					captureExtra();
					
					
					
					updateGui();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					disc();
				}
			}
		});
	}
	
	public void captureExtra() throws IOException, ClassNotFoundException {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		
		ByteArrayInputStream in = new ByteArrayInputStream(Main.mcuSocket.cmd(Cmd.CMD_GET_CAPTURE_EXTRA_INFO));
	    ObjectInputStream is = new ObjectInputStream(in);
	    ((ExtraPic)is.readObject()).draw(g);
		
		// new ExtraPic().setByteArray(Main.mcuSocket.cmd(Cmd.CMD_GET_CAPTURE_EXTRA_INFO)).draw(g);
		
		
		
		image[0] = SwingFXUtils.toFXImage(bufferedImage, null);
	}
	
	static abstract class MyChangeListener implements ChangeListener<Boolean> {
		protected final int index;
		public MyChangeListener(int index) {
			this.index = index;
		}
	}

}
