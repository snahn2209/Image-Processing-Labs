// BV Ue3 WS2022/23 Vorgabe
//
// Copyright (C) 2022 by Klaus Jung
// All rights reserved.
// Date: 2022-09-20
 		   		    	 

package bv_ws2223;

public class MorphologicFilter {
 		   		    	 
	// filter implementations go here:
	
	public void copy(RasterImage src, RasterImage dst) {
		// TODO: just copy the image
		System.arraycopy(src.argb, 0, dst.argb, 0, src.argb.length);
	}
	
	public void dilation(RasterImage src, RasterImage dst, boolean[][] kernel) {
		// kernel's first dimension: y (row), second dimension: x (column)
		// TODO: dilate the image using the given kernel
		copy(src,dst);

		for (int y = 0; y < dst.height; y++) {
			for (int x = 0; x < dst.width; x++) {
				int pos = y*dst.width + x;

				int pix= src.argb[pos];

				if(pix==0xff000000){
					//schleife über kernel
					for(int yK=0; yK<kernel.length;yK++){
						for( int xK=0; xK<kernel[yK].length; xK++){

							if(kernel[yK][xK]==true){
								// y+yK-(kernel.length/2)-> in eigene variablen schreiben, testen ob <0 oder >height, selbes mit x
								int posKernelPic = (y+yK-(kernel.length/2)) * dst.width + (x+xK-(kernel[yK].length/2));

								if(posKernelPic>0 && posKernelPic <dst.argb.length-1){

									dst.argb[posKernelPic]=0xff000000;
}
							}
						}
					}

				}
			}
		}
	}
 		   		    	 
	public void erosion(RasterImage src, RasterImage dst, boolean[][] kernel) {
		// This is already implemented. Nothing to do.
		// It will function once you implemented dilation and RasterImage invert()
		src.invert();
		dilation(src, dst, kernel);
		dst.invert();
		src.invert();
	}
	
	public void opening(RasterImage src, RasterImage dst, boolean[][] kernel) {
		// TODO: implement opening by using dilation() and erosion()
		RasterImage tmp = new RasterImage(src.width, src.height);

		dilation(src,tmp,kernel);
		erosion(tmp,dst,kernel);

	}
	
	public void closing(RasterImage src, RasterImage dst, boolean[][] kernel) {
		// TODO: implement closing by using dilation() and erosion()
		RasterImage tmp = new RasterImage(src.width, src.height);

		erosion(src,tmp,kernel);
		dilation(tmp,dst,kernel);
	}
	
	
 		   		    	 	
	

}
 		   		    	 




