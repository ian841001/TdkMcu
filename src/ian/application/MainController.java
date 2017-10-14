package ian.application;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import ian.main.AllData;
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
	
	@FXML Label rpiLbl0, rpiLbl1;
	
	@FXML Label mwcLblDebug0, mwcLblDebug1, mwcLblDebug2, mwcLblDebug3;
	Label[] mwcLblDebug;
	
	@FXML Label rpiLblDebug0, rpiLblDebug1, rpiLblDebug2, rpiLblDebug3, rpiLblDebug4, rpiLblDebug5, rpiLblDebug6, rpiLblDebug7;
	Label[] rpiLblDebug;
	
	@FXML TextField extraInfoTxt0, extraInfoTxt1, extraInfoTxt2, extraInfoTxt3, extraInfoTxt4, extraInfoTxt5, extraInfoTxt6, extraInfoTxt7;
	@FXML Label extraInfoLbl0, extraInfoLbl1, extraInfoLbl2, extraInfoLbl3, extraInfoLbl4, extraInfoLbl5, extraInfoLbl6, extraInfoLbl7;
	TextField[] extraInfoTxt;
	Label[] extraInfoLbl;
	
	@FXML Label msgLbl;
	
	@FXML ImageView imageView0;
	ImageView[] imageView;
	
	@FXML Label capLbl0, capLbl1, capLbl2, capLbl3;
	Label[] capLbl;
	
	@FXML Label extraMsgLbl0;
	
	@FXML Label mcuModeLbl0, mcuModeLbl1, mcuModeLbl2, mcuModeLbl3, mcuModeLbl4;
	
	@FXML Label tempatureLbl;
	
	@FXML Label detailLbl0, detailLbl1, detailLbl2, detailLbl3, detailLbl4,detailLbl5;
	
	

	private AllData info = new AllData();
	
	
	private BufferedImage bufferedImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
	private Graphics2D g = bufferedImage.createGraphics();
	private Image[] image;
	
	private Repeater repeater;
	
//	private int cycleTime;
//	private int step, setWantAlt;
//	
//	private byte level;
//	private int[] debug = new int[8];
	private byte[] extraInfo = new byte[8];
	
	
//	private String msgStr = "message from rpi";
//	private String extraMsgStr = "message from rpi";
	
	
	
	
	private void updateLblStatus() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lblStatus.setText(new String[]{"Disconnected.","Connecting...","Connected.","ReConnnect..."}[Main.mcuSocket.mode()]);
				lblStatus.setStyle("-fx-background-color: " + new String[]{"#FF8080;","#FFFF00;","#66FF66;","#5599FF;"}[Main.mcuSocket.mode()]);
				if (Main.mcuSocket.mode() == 2) {
					repeater.start();
				}
			}
		});
	}
	
	private void updateGui() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < motorRec.length; i++) {
					motorRec[i].setWidth((info.motor[i] - 1000) / 2);
					motorLbl[i].setText(String.valueOf(info.motor[i]));
				}
				
				for (int i = 0; i < rcRec.length; i++) {
					rcRec[i].setWidth((info.rc[i] - 1000) / 10);
					rcLbl[i].setText(String.valueOf(info.rc[i]));
				}
				
				altLbl.setText(String.valueOf(info.altEstAlt));
				
				modeLbl1.setStyle("-fx-background-color: " + (info.ok_to_arm  ? "green" : "red"));
				modeLbl2.setStyle("-fx-background-color: " + (info.angle_mode ? "green" : "red"));
				modeLbl3.setStyle("-fx-background-color: " + (info.armed      ? "green" : "red"));
				modeLbl4.setStyle("-fx-background-color: " + (info.baro_mode  ? "green" : "red"));
				altLbl.setStyle("-fx-background-color: " + (info.isSonarOk  ? "green" : "red"));
				
				mcuModeLbl0.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[info.armMode  ]));
				mcuModeLbl1.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[info.baroMode ]));
				mcuModeLbl2.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[info.yawMode  ]));
				mcuModeLbl3.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[info.rollMode ]));
				mcuModeLbl4.setStyle("-fx-background-color: " + (new String[]{"red", "yellow", "green"}[info.pitchMode]));
				
				
				
				rpiLbl0.setText(String.valueOf(info.step));
				rpiLbl1.setText(String.valueOf(info.cycleTime));
				
				detailLbl0.setText(String.valueOf(info.setWantAlt));
				detailLbl1.setText(String.valueOf(info.altHold));
				detailLbl2.setText(String.valueOf(info.att[2]));
				detailLbl3.setText(String.valueOf(info.takeOffHeading));
				detailLbl4.setText(String.valueOf(info.wantHeading));
				detailLbl5.setText(String.valueOf(info.altBaro));
				
				
				for (int i = 0; i < mwcLblDebug.length; i++) {
					mwcLblDebug[i].setText(String.valueOf(info.debug[i]));
				}
				
				for (int i = 0; i < rpiLblDebug.length; i++) {
					rpiLblDebug[i].setText(String.valueOf(info.rpiDebug[i]));
				}
				
				
				msgLbl.setStyle("-fx-background-color: " + (new String[]{"#66FF66", "#FFFF00", "#FF8080"}[info.msgStruct.level]));
				msgLbl.setText(info.msgStruct.msgStr);
				
				tempatureLbl.setText(String.format("%.1f'C", (float)info.tempature / 10));
				
				extraMsgLbl0.setText(info.extraMsg);
				
				for (int i = 0; i < extraInfoLbl.length; i++) {
					extraInfoLbl[i].setText(String.valueOf(extraInfo[i]));
				}
				
				
				
				int index = 0;
				short[] data = info.captureExtraInfo;
				int len;
				
				try {
					g.setStroke(new BasicStroke(1));
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
					
					if (data != null) {
						if (data.length > index) {
							g.setColor(Color.WHITE);
							len = data[index++];
							for (int i = 0; i < len; i++) {
								g.drawRect(data[index++], data[index++], 1, 1);
							}
						}
						
						if (data.length > index) {
							g.setStroke(new BasicStroke(3));
							g.setColor(Color.GREEN);
							len = data[index++];
							for (int i = 0; i < len; i++) {
								g.drawLine(data[index++], data[index++], data[index++], data[index++]);
							}
						}
						
						if (data.length > index) {
							g.setColor(Color.RED);
							len = data[index++];
							for (int i = 0; i < len; i++) {
								g.drawLine(data[index++], data[index++], data[index++], data[index++]);
							}
						}
						
						if (data.length > index) {
							g.setStroke(new BasicStroke(1));
							g.setColor(Color.YELLOW);
							len = data[index++];
							for (int i = 0; i < len; i++) {
								g.drawLine(data[index++], data[index++], data[index++], data[index++]);
							}
						}
						
						if (data.length > index) {
							g.setStroke(new BasicStroke(3));
							g.setColor(Color.BLUE);
							len = data[index++];
							for (int i = 0; i < len; i++) {
								short x = data[index++];
								short y = data[index++];
								short r = data[index++];
								g.drawOval(x - r, y - r, 2 * r, 2 * r);
							}
						}
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
				}
				
				
				
				
				image[0] = SwingFXUtils.toFXImage(bufferedImage, null);
				
				
				
				
				
				
				for (int i = 0; i < image.length; i++) {
					if (image[i] != null) {
						imageView[i].setImage(image[i]);
					}
				}
				
				
				capLbl0.setText(String.valueOf(info.captureStatus));
				capLbl1.setText(String.valueOf(info.captureDeltaX));
				capLbl2.setText(String.valueOf(info.captureDeltaY));
				capLbl3.setText(String.valueOf(info.captureAngle));
				
				
				
			}
		});
	}
	
	private void conn() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Main.mcuSocket.conn(txtIp.getText(), Integer.valueOf(txtPort.getText()));
				} catch (IOException e) {
					e.printStackTrace();
					updateLblStatus();
				}
			}
		}).start();
	}
	private void disc() {
		try {
			repeater.stop();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Main.mcuSocket.disc();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		imageView = new ImageView[]{imageView0};
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
		updateLblStatus();
		
		Main.mcuSocket.setModeChangedListener(new Runnable() {
			
			@Override
			public void run() {
				updateLblStatus();
			}
		});
		
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
					try {
						info = ((AllData)Main.mcuSocket.cmdObj(Cmd.CMD_GET_INFO));
						Main.mcuSocket.cmd(Cmd.CMD_SET_STATUS, extraInfo);
						updateGui();
					} catch (SocketTimeoutException e) {
						if (e.getMessage().equals("Read timed out")) {
							disc();
							Main.mcuSocket.reservet();
						} else {
							throw e;
						}
					}	
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					disc();
				}
			}
		});
	}
	
	
	static abstract class MyChangeListener implements ChangeListener<Boolean> {
		protected final int index;
		public MyChangeListener(int index) {
			this.index = index;
		}
	}

}
