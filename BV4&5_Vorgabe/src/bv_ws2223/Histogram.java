// BV Ue4 WS2022/23 Vorgabe
//
// Copyright (C) 2022 by Klaus Jung
// All rights reserved.
// Date: 2022-09-20
 		   		    	 

package bv_ws2223;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;

public class Histogram {
 		   		    	 
	private static final int grayLevels = 256;
	
    private GraphicsContext gc;
    private int maxHeight;

	private int MaxValue;
    
    private int[] histogram = new int[grayLevels];
 		   		    	 
    public Histogram() {
	}
    
	public Histogram(GraphicsContext gc, int maxHeight) {
		this.gc = gc;
		this.maxHeight = maxHeight;
	}
	
	public int[] getValues() {
		return histogram;
	}

	public void setImageRegion(RasterImage image, int regionStartX, int regionStartY, int regionWidth, int regionHeight) {
		histogram = new int[grayLevels];
		MaxValue=0;
		// TODO: calculate histogram[] out of the gray values found the given image region
		for (int yPos=regionStartY; yPos<regionStartY+regionHeight; yPos++){
			for(int xPos= regionStartX; xPos<regionStartX+regionWidth; xPos++){
				int x=xPos;
				int y=yPos;

				//randbehandlung
				if(x<0){x=0;}
				if(x>= image.width){x=image.width-1;}
				if(y<0){y=0;}
				if(y>= image.height){y=image.height-1;}

				//graustufe von jedem Pixel nehmen
				int pos= y*image.width+x;
				int grayValue = image.argb[pos];
				int value = (grayValue >> 16) & 0xff;
				//histogramm an der stelle des Graustufenwerts +1
				histogram[value] += 1;
				//höchsten Wert des Arrays besrtimmt für Normierung
				if(histogram[value]>MaxValue){
					MaxValue = histogram[value];
				}
			}

		}

	}
	
	public Integer getMinimum() {
		// Will be used in Exercise 5.
		for (int j=0; j< histogram.length;j++){
			if(histogram[j]!=0) return j;
		}
		return 0;
	}

	public Integer getMaximum() {
		// Will be used in Exercise 5.
		for(int j= histogram.length-1;j>=0;j--){
			if(histogram[j]!=0){
				return j;
			}
		}
		return 255;

	}
 		   		    	 
	public Double getMean() {
		// Will be used in Exercise 5.
		double mean=0;
		int count=0;
		for(int j=0; j< histogram.length;j++){
			if(histogram[j]!=0) {
				count+=histogram[j];
				mean = mean + j * histogram[j];
			}
		}
		return mean/(double)count;
	}
 		   		    	 
	public Integer getMedian() {
		int[] histoSorted= new int[histogram.length];
		System.arraycopy(histogram, 0, histoSorted, 0, histogram.length);
		Arrays.sort(histoSorted);
		int mid=histoSorted.length/2;

		return histoSorted[mid];
	}
 		   		    	 
	public Double getVariance() {
		// Will be used in Exercise 5.
		int count=0;
		double sigma = 0;
		double mean=this.getMean();
		for(int j =0; j< histogram.length;j++){
			if(histogram[j]!=0){
				count+=histogram[j];
			}
		}
		for (int j=0; j< histogram.length;j++){
			if(histogram[j]!=0){
				double temp=(Math.pow(j-mean, 2))*(histogram[j])/count;
				sigma+=temp;
			}
		}
		return sigma;
	}
 		   		    	 
	public Double getEntropy() {
		// Will be used in Exercise 5.
		double entropy=0;
		double p;
		int count=0;
		for(int j=0;j< histogram.length;j++){
			if(histogram[j]!=0){
				count+=histogram[j];
			}
		}
		for(int j=0; j<histogram.length;j++){
			if(histogram[j]!=0){
				p=(double)histogram[j]/(double)count;
				entropy+=p*(Math.log(p)/Math.log(2));
			}
		}
		return -entropy;
	}
 		   		    	 
	public void draw(Color lineColor) {
		if(gc == null) return;
		gc.clearRect(0, 0, grayLevels, maxHeight);
		gc.setStroke(lineColor);
		gc.setLineWidth(1);
 		   		    	 
		// TODO: draw histogram[] into the gc graphic context
		// Note that we need to add 0.5 to all coordinates to align points to pixel centers 
		
		double shift = 0.5;
		//für jeden Eintrag im Array eine Linie zeichnen
		for(int i=0; i< histogram.length; i++){
			//Wert normieren
			int wert = (int) (histogram[i]*((float)maxHeight/MaxValue));
			gc.strokeLine(i+shift, maxHeight+shift, i+shift, maxHeight-wert+shift);
		}
		// Remark: This is some dummy code to give you an idea for line drawing		
		//gc.strokeLine(shift, shift, grayLevels-1 + shift, maxHeight-1 + shift);
		//gc.strokeLine(grayLevels-1 + shift, shift, shift, maxHeight-1 + shift);
		
	}
 		   		    	 
}
 		   		    	 






