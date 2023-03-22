// BV Ue1 WS2022/23 Vorgabe
//
// Copyright (C) 2022 by Klaus Jung
// All rights reserved.
// Date: 2022-09-20
 		   		    	 

package bv_ws2223;

public class GaussFilter {

	private double[][] kernel;

	public double[][] getKernel() {
		return kernel;
	}

	public void apply(RasterImage src, RasterImage dst, int kernelSize, double sigma) {

		// TODO: Implement a Gauss filter of size "kernelSize" x "kernelSize" with given "sigma"

		// Step 1: Allocate appropriate memory for the field variable "kernel" representing a 2D array.
		kernel = new double[kernelSize][kernelSize];

		// Step 2: Fill in appropriate values into the "kernel" array.
		// Hint:
		// Use g(d) = e^(- d^2 / (2 * sigma^2)), where d is the distance of a coefficient's position to the hot spot.
		// Note that in this comment e^ denotes the exponential function and ^2 the square. In Java ^ is a different operator.
		double summe = 0;

		if (kernelSize == 1) {
			kernel[0][0] = 1;

			for (int y = 0; y < src.height; y++) {
				for (int x = 0; x < src.width; x++) {

					int pos = y * src.width + x;

					dst.argb[pos] = src.argb[pos]; //kann man weglassen
				}
			}
		}
		//double summe=0;

		else{
			int hotspot = kernelSize / 2;

			for (int xKernel = -hotspot; xKernel <= hotspot; xKernel++) {
				//hotspot in x,y -> 0,0
				// kernel ist -hotspot bis hotspot in x,y
				for (int yKernel = -hotspot; yKernel <= hotspot; yKernel++) {
					//positionen im kernel
					int xPosK = hotspot + xKernel;
					int yPosK = hotspot + yKernel;

					double d = Math.sqrt(xKernel * xKernel + yKernel * yKernel);
					double g = Math.exp(-1*(Math.pow(d, 2)) / (2 * Math.pow(sigma, 2)));
					//Math.exp takes E as base

					kernel[xPosK][yPosK] = g;

					summe += g;
					//summe = summe+g;
				}
			}

			// Step 3: Normalize the "kernel" such that the sum of all its values is one.
			for (int i = 0; i < kernelSize; i++) {
				for (int j = 0; j < kernelSize; j++) {
					kernel[i][j] = kernel[i][j] / summe;
				}
			}


			// Step 4: Apply the filter given by "kernel" to the source image "src". The result goes to image "dst".
			// Use "constant continuation" for boundary processing.
			//Schleifen über das Bild
			for (int y = 0; y < src.height; y++) {
				for (int x = 0; x < src.width; x++) {

					double rn = 0;
					double gn = 0;
					double bn = 0;

					int pos = y * src.width + x; //position im Bild

					int yPos = -1; //y-pos im Kernel

					//Schleifen über den Kernel
					for (int yK = y - hotspot; yK <= y + hotspot; yK++) {

						//constant continuation
						int yKR = yK;

						//oben
						if (yKR < 0) { yKR = 0; } //zb bei beginn bei 0,0 auf dem bild , kernel schleife beginnt bei minus-- Hotspot an, dh <0
						//unten
						if (yKR > src.height - 1) { yKR = src.height - 1;}

						yPos++;

						int xPos = -1;
						for (int xK = x - hotspot; xK <= x + hotspot; xK++) {

							int xKR = xK;
							//links
							if (xKR < 0) { xKR = 0;}
							//rechts
							if (xKR > src.width - 1) { xKR = src.width - 1;}

							xPos++;

							int newPos = yKR * src.width + xKR; //neue position berechnen da randwerte "neu"
							int rgbNew = src.argb[newPos]; //rgb wert von den neuen Koordinaten nehmen aus src bild

							//in r, g und b variablen unterteilen
							int r = ((rgbNew >> 16) & 0xff);
							int g = ((rgbNew >> 8) & 0xff);
							int b = (rgbNew & 0xff);

							//neue rgb werte mit den werten aus dem kernel berechnen
							rn +=  (r * kernel[yPos][xPos]);
							gn +=  (g * kernel[yPos][xPos]);
							bn +=  (b * kernel[yPos][xPos]);

							int rnew = (int)rn;
							int gnew = (int)gn;
							int bnew = (int)bn;


							dst.argb[pos] = (0xFF << 24) | (rnew << 16) | (gnew << 8) | bnew;

						}
					}
				}
			}
		}
	}
}
		   		     	








