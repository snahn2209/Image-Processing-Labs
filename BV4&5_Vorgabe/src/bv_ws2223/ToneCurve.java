// BV Ue4 WS2022/23 Vorgabe
//
// Copyright (C) 2022 by Klaus Jung
// All rights reserved.
// Date: 2022-09-20
 		   		    	 

package bv_ws2223;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ToneCurve {
 		   		    	 
	private static final int grayLevels = 256;
	
    private GraphicsContext gc;
    
    private int[] grayTable = new int[grayLevels];
 		   		    	 
	public int[] getGrayTable() {
		return grayTable;
	}

	public ToneCurve(GraphicsContext gc) {
		this.gc = gc;
	}
	
	public void updateTable(int minInput, int maxInput, int minOutput, int maxOutput) {
		
		// TODO: Fill the grayTable[] array to map gay input values to gray output values.
		// It will be used as follows: grayOut = grayTable[grayIn].
		//
		// Use minInput, maxInput, minOutput, and maxOutput settings.
		for(int x0= 0; x0<minInput; x0++){
			grayTable[x0] = 255-minOutput;
		}
		for(int x1= maxInput; x1<=255; x1++) {
			grayTable[x1] = 255-maxOutput;
		}
		for(int i=minInput; i<maxInput; i++){
			grayTable[i]=(int) (255-(((float)(maxOutput-minOutput) / (maxInput-minInput)) * (i-minInput) + minOutput)) ;
		}

	}
	
	public void applyTo(RasterImage image) {
		
		// TODO: apply the gray value mapping to the given image
		// graustufe durch wert aus grayTable ersetzen an der stelle

		for(int y=0;y<image.height;y++){
			for(int x=0; x<image.width; x++){
				int pos = y* image.width+x;
				int grayValue = image.argb[pos];

				int value = (grayValue >> 16) & 0xff;

				value= 255-grayTable[value];

				image.argb[pos] = (0xFF<<24) | (value<<16) | (value<<8) | value;

			}
		}
	}
	
	public void draw(Color lineColor) {
		if(gc == null) return;
		gc.clearRect(0, 0, grayLevels, grayLevels);
		gc.setStroke(lineColor);
		gc.setLineWidth(3);
		
		// TODO: draw the tone curve into the gc graphic context
		// Note that we need to add 0.5 to all coordinates to align points to pixel centers 
		
		double shift = 0.5;

		// Remark: This is some dummy code to give you an idea for graphics drawing using paths		
		gc.beginPath();
		//für jeden Eintrag in grayTable[] Linie zeichnen
		for(int i = 0; i< grayTable.length;i++){
			gc.lineTo(i+shift,grayTable[i]+shift);
		}
		gc.stroke();

	}

 		   		    	 
}
 		   		    	 




