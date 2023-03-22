// BV Ue2 WS2022/23 Vorgabe
//
// Copyright (C) 2022 by Klaus Jung
// All rights reserved.
// Date: 2022-09-20
 		   		    	 

package bv_ws2223;


import static java.lang.Math.cos;

public class GeometricTransform {
 		   		    	 
	public enum InterpolationType { 
		NEAREST("Nearest Neighbour"), 
		BILINEAR("Bilinear");
		
		private final String name;       
	    private InterpolationType(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
	public void perspective(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion, InterpolationType interpolation) {
		switch(interpolation) {
		case NEAREST:
			perspectiveNearestNeighbour(src, dst, angle, perspectiveDistortion);
			break;
		case BILINEAR:
			perspectiveBilinear(src, dst, angle, perspectiveDistortion);
			break;
		default:
			break;	
		}
		
	}
 		   		    	 
	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveNearestNeighbour(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
 		   		    	 
		// TODO: implement the geometric transformation using nearest neighbour image rendering


		for (int xDst=0; xDst< dst.width;xDst++){
			for (int yDst=0;yDst< dst.height;yDst++){

				int posDest = yDst * dst.width + xDst;


				double xDT = xDst- (dst.width/2);
				double yDT = yDst-(dst.height/2);

				double radiant = angle * Math.PI/180;

				double yST = yDT / (Math.cos(radiant) - yDT * perspectiveDistortion * Math.sin(radiant)); //cos und sin vor die schleife setzen
				double xST = xDT * (perspectiveDistortion * Math.sin(radiant) * yST + 1); //klammer als variable vor den for-schleifen deklarieren

				double xSrc= xST + src.width/2.0;
				double ySrc= yST + src.height/2.0;

				int xSI= (int) Math.round(xSrc);
				int ySI= (int) Math.round(ySrc);
				int srcPos= src.width*ySI+xSI;


				if(xSI<0|| xSI>= src.width||ySI<0||ySI>=src.height){
					dst.argb[posDest]= 0xFFFFFFFF;
				}else {
					dst.argb[posDest] = src.argb[srcPos];
				}
			}
		}
		
		// NOTE: angle contains the angle in degrees, whereas Math trigonometric functions need the angle in radiant
		
	}


	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveBilinear(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		double radiant = angle * Math.PI / 180;
		// TODO: implement the geometric transformation using bilinear interpolation
		for (int xDst=0; xDst< dst.width;xDst++) {
			for (int yDst = 0; yDst < dst.height ; yDst++) {

				int posDest = yDst * dst.width + xDst;


				double xDT = xDst - (dst.width / 2);
				double yDT = yDst - (dst.height / 2);

				//double radiant = angle * Math.PI / 180;

				double yST = yDT / (Math.cos(radiant) - yDT * perspectiveDistortion * Math.sin(radiant));
				double xST = xDT * (perspectiveDistortion * Math.sin(radiant) * yST + 1);

				double xSrc = xST + src.width/2.0;
				int xSFloor = (int) Math.floor(xSrc); //math.floor Returns closest to positive infinity int

				double ySrc = yST + src.height/2.0;
				int ySFloor = (int) Math.floor(ySrc); //cast wäre an sich schneller, bei neg. falsch

				double h = xSrc - xSFloor; //horizantaler abstand zum pixel
				double v = ySrc - ySFloor; //vertikal

				int rgb1 = 0;
				int rgb2 = 0;
				int rgb3 = 0;
				int rgb4 = 0;

				//[3][4]
				//[1][2]

				//randbehandlung nicht richtig
				if(ySFloor >= src.height-1 || xSFloor >= src.width-1 || ySFloor<0 || xSFloor<0){
					rgb1 = 0xFFFFFFFF;
					rgb2 = 0xFFFFFFFF;
					rgb3 = 0xFFFFFFFF;
					rgb4 = 0xFFFFFFFF;
				}else{
					rgb1 = src.argb[ySFloor * src.width + xSFloor];
					rgb2 = src.argb[ySFloor * src.width + xSFloor+1];
					rgb3 = src.argb[(ySFloor+1) * src.width + xSFloor];
					rgb4 = src.argb[(ySFloor+1) * src.width + xSFloor+1];
				}


				//rgb Werte von den einzelnen Pixeln holen
				int r1 = (rgb1 >> 16) & 0xff; int g1 = (rgb1 >> 8) & 0xff; int b1 = rgb1 & 0xff;
				int r2 = (rgb2 >> 16) & 0xff; int g2 = (rgb2 >> 8) & 0xff; int b2 = rgb2 & 0xff;
				int r3 = (rgb3 >> 16) & 0xff; int g3 = (rgb3 >> 8) & 0xff; int b3 = rgb3 & 0xff;
				int r4 = (rgb4 >> 16) & 0xff; int g4 = (rgb4 >> 8) & 0xff; int b4 = rgb4 & 0xff;

				//Formel aus gdm Folien
				int rn = (int) (r1 * (1 - h) * (1 - v) + r2 * h * (1 - v) + r3 * (1 - h) * v + r4 * h * v);
				int gn = (int) (g1 * (1 - h) * (1 - v) + g2 * h * (1 - v) + g3 * (1 - h) * v + g4 * h * v);
				int bn = (int) (b1 * (1 - h) * (1 - v) + b2 * h * (1 - v) + b3 * (1 - h) * v + b4 * h * v);

				dst.argb[posDest] = (0xff << 24) | (rn << 16) | (gn << 8) | (bn);

			}
		}
		// NOTE: angle contains the angle in degrees, whereas Math trigonometric functions need the angle in radiant
		
 	}

 		   		    	 
}
 		   		    	 



