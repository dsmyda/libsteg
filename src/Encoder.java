import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.awt.Point;

public class Encoder {

	private BufferedImage img;
	public static final int EOF = 255;
	
	public Encoder(BufferedImage img) {
		this.img = img;
	}
	
	public BufferedImage hideString(String s) throws IOException {
		byte[] org = extractBytes(this.img);
		ImageEncoder ie = new ImageEncoder(org);
		byte[] sBytes = s.getBytes();
		
		for(byte b: sBytes) {
			for(int i = 7; i >= 0; i--) {
				ie.changeNthBit(getBit(b,i--), 0);
				ie.changeNthBit(getBit(b,i), 1);
				ie.nextIndex();
			}
		}
		
		ie.addEOF();
		
		BufferedImage secretImage=new BufferedImage(this.img.getWidth(), this.img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		secretImage.setData(Raster.createRaster(secretImage.getSampleModel(), new DataBufferByte(ie.getImg(), ie.getImg().length), new Point() ) );
		return secretImage;
	}
	
	public String decodeImage(BufferedImage bi) {
		byte[] biBytes = extractBytes(bi);
		StringBuilder msg = new StringBuilder();
		
		String b = "";
		for(int i = 0; i < biBytes.length; i++) {
			b += getBit(biBytes[i],0) +""+ getBit(biBytes[i],1);
			
			//(i+1)*2 % 8 - calculate all the times we've appended 8 bits
			if((i+1)*2 % 8 == 0) {
				if (Integer.parseInt(b, 2) == EOF) break;
				msg.append((char) Integer.parseInt(b,2));
				b = "";
			}
		}
		
		return msg.toString();
	}
	
	private byte getBit(byte b, int pos) {
		return (byte) ((b >> pos) & 1);
	}
	
	private byte[] extractBytes(BufferedImage img) {
		 WritableRaster raster = img.getRaster();
		 DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
		 return data.getData();
	}
}