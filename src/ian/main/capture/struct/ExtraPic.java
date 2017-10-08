package ian.main.capture.struct;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;

public class ExtraPic implements Serializable {
	private static final long serialVersionUID = 1;
	
	private ArrayList<Short> whitePointX = new ArrayList<>();
	private ArrayList<Short> whitePointY = new ArrayList<>();

	private ArrayList<Short> greenLineX1 = new ArrayList<>();
	private ArrayList<Short> greenLineY1 = new ArrayList<>();
	private ArrayList<Short> greenLineX2 = new ArrayList<>();
	private ArrayList<Short> greenLineY2 = new ArrayList<>();

	private ArrayList<Short> redLineX1 = new ArrayList<>();
	private ArrayList<Short> redLineY1 = new ArrayList<>();
	private ArrayList<Short> redLineX2 = new ArrayList<>();
	private ArrayList<Short> redLineY2 = new ArrayList<>();

	private ArrayList<Short> yellowLineX1 = new ArrayList<>();
	private ArrayList<Short> yellowLineY1 = new ArrayList<>();
	private ArrayList<Short> yellowLineX2 = new ArrayList<>();
	private ArrayList<Short> yellowLineY2 = new ArrayList<>();

	private ArrayList<Short> blueCircleX = new ArrayList<>();
	private ArrayList<Short> blueCircleY = new ArrayList<>();
	private ArrayList<Short> blueCircleR = new ArrayList<>();

	public void addWhitePoint(short x, short y) {
		whitePointX.add(x);
		whitePointY.add(y);
	}

	public void addGreenLine(short x1, short y1, short x2, short y2) {
		greenLineX1.add(x1);
		greenLineY1.add(y1);
		greenLineX2.add(x2);
		greenLineY2.add(y2);
	}

	public void addRedLine(short x1, short y1, short x2, short y2) {
		redLineX1.add(x1);
		redLineY1.add(y1);
		redLineX2.add(x2);
		redLineY2.add(y2);
	}

	public void addYellowLine(short x1, short y1, short x2, short y2) {
		yellowLineX1.add(x1);
		yellowLineY1.add(y1);
		yellowLineX2.add(x2);
		yellowLineY2.add(y2);
	}

	public void addBlueCircle(short x, short y, short r) {
		blueCircleX.add(x);
		blueCircleY.add(y);
		blueCircleR.add(r);
	}

	public void draw(Graphics2D g) {

		g.setStroke(new BasicStroke(1));

		g.setColor(Color.WHITE);
		for (int i = 0; i < whitePointX.size(); i++) {
			g.drawRect(whitePointX.get(i), whitePointY.get(i), 1, 1);
		}

		g.setStroke(new BasicStroke(3));

		g.setColor(Color.GREEN);
		for (int i = 0; i < greenLineX1.size(); i++) {
			g.drawLine(greenLineX1.get(i), greenLineY1.get(i), greenLineX2.get(i), greenLineY2.get(i));
		}

		g.setColor(Color.RED);
		for (int i = 0; i < redLineX1.size(); i++) {
			g.drawLine(redLineX1.get(i), redLineY1.get(i), redLineX2.get(i), redLineY2.get(i));
		}

		g.setStroke(new BasicStroke(1));

		g.setColor(Color.YELLOW);
		for (int i = 0; i < yellowLineX1.size(); i++) {
			g.drawLine(yellowLineX1.get(i), yellowLineY1.get(i), yellowLineX2.get(i), yellowLineY2.get(i));
		}

		g.setColor(Color.BLUE);
		for (int i = 0; i < blueCircleX.size(); i++) {
			short r = blueCircleR.get(i);
			g.drawOval(blueCircleX.get(i) - r, blueCircleY.get(i) - r, 2 * r, 2 * r);
		}
	}

//	public byte[] getByteArray() throws IOException {
//		int len = whitePointX.size() * 4 + (greenLineX1.size() + redLineX1.size() + yellowLineX1.size()) * 8
//				+ blueCircleX.size() * 6 + 10;
//		ByteBuffer buffer = ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN);
//
//		buffer.putShort((short) whitePointX.size());
//		for (int i = 0; i < whitePointX.size(); i++) {
//			buffer.putShort(whitePointX.get(i)).putShort(whitePointY.get(i));
//		}
//
//		buffer.putShort((short) greenLineX1.size());
//		for (int i = 0; i < greenLineX1.size(); i++) {
//			buffer.putShort(greenLineX1.get(i)).putShort(greenLineY1.get(i));
//			buffer.putShort(greenLineX2.get(i)).putShort(greenLineY2.get(i));
//		}
//
//		buffer.putShort((short) redLineX1.size());
//		for (int i = 0; i < redLineX1.size(); i++) {
//			buffer.putShort(redLineX1.get(i)).putShort(redLineY1.get(i));
//			buffer.putShort(redLineX2.get(i)).putShort(redLineY2.get(i));
//		}
//
//		buffer.putShort((short) yellowLineX1.size());
//		for (int i = 0; i < yellowLineX1.size(); i++) {
//			buffer.putShort(yellowLineX1.get(i)).putShort(yellowLineY1.get(i));
//			buffer.putShort(yellowLineX2.get(i)).putShort(yellowLineY2.get(i));
//		}
//
//		buffer.putShort((short) blueCircleX.size());
//		for (int i = 0; i < blueCircleX.size(); i++) {
//			buffer.putShort(blueCircleX.get(i)).putShort(blueCircleY.get(i));
//			buffer.putShort(blueCircleR.get(i));
//		}
//
//		return buffer.array();
//	}
//
//	public ExtraPic setByteArray(byte[] data) {
//		if (data.length == 0)
//			return this;
//		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
//		int len;
//
//		len = buffer.getShort();
//		for (int i = 0; i < len; i++) {
//			whitePointX.add(buffer.getShort());
//			whitePointY.add(buffer.getShort());
//		}
//
//		len = buffer.getShort();
//		for (int i = 0; i < len; i++) {
//			greenLineX1.add(buffer.getShort());
//			greenLineY1.add(buffer.getShort());
//			greenLineX2.add(buffer.getShort());
//			greenLineY2.add(buffer.getShort());
//		}
//
//		len = buffer.getShort();
//		for (int i = 0; i < len; i++) {
//			redLineX1.add(buffer.getShort());
//			redLineY1.add(buffer.getShort());
//			redLineX2.add(buffer.getShort());
//			redLineY2.add(buffer.getShort());
//		}
//
//		len = buffer.getShort();
//		for (int i = 0; i < len; i++) {
//			yellowLineX1.add(buffer.getShort());
//			yellowLineY1.add(buffer.getShort());
//			yellowLineX2.add(buffer.getShort());
//			yellowLineY2.add(buffer.getShort());
//		}
//
//		len = buffer.getShort();
//		for (int i = 0; i < len; i++) {
//			blueCircleX.add(buffer.getShort());
//			blueCircleY.add(buffer.getShort());
//			blueCircleR.add(buffer.getShort());
//		}
//		return this;
//	}
}
