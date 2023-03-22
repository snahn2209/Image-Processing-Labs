// BV Ue1 WS2022/23 Vorgabe
//
// Copyright (C) 2022 by Klaus Jung
// All rights reserved.
// Date: 2022-09-20
 		   		    	 

package bv_ws2223;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

public class FilterAppController {
 		   		    	 
    private static final String initialFileName = "ara_klein.jpg";
	private static File fileOpenPath = new File(".");

	private int kernelSize;
	private double sigma;
	private double[][] kernel;
	
	private RasterImage origImg;

    @FXML
    private Slider kernelSizeSlider;

    @FXML
    private Slider sigmaSlider;

    @FXML
    private Label kernelSizeLabel;

    @FXML
    private Label sigmaLabel;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label zoomLabel;

    @FXML
    private TextArea kernelInfoTextArea;

    @FXML
    private Canvas kernelInfoCanvas;

    @FXML
    private ImageView originalImageView;

    @FXML
    private ScrollPane originalScrollPane;

    @FXML
    private ImageView filteredImageView;

    @FXML
    private ScrollPane filteredScrollPane;

    @FXML
    private Label messageLabel;
 		   		    	 
    @FXML
    void openImage() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(fileOpenPath); 
    	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
    	File selectedFile = fileChooser.showOpenDialog(null);
    	if(selectedFile != null) {
        	zoomSlider.setValue(1);
    		zoomChanged();
    		fileOpenPath = selectedFile.getParentFile();
    		origImg = new RasterImage(selectedFile);
    		processImages();
    		messageLabel.getScene().getWindow().sizeToScene();;
    	}
    }

    @FXML
    void sigmaChanged() {
    	double newSigma = sigmaSlider.getValue();
    	if(newSigma != sigma) {
        	processImages();    		
    	}
    }

    @FXML
    void kernelSizeChanged() {
    	int newSize = (int)kernelSizeSlider.getValue() | 1; // ensure odd integer value
    	if(newSize != kernelSize) {
        	processImages();    		
    	}
    }

    Point2D mousePoint;
    
    @FXML
    void mousePressed(MouseEvent event) {
    	mousePoint = new Point2D(event.getX(), event.getY());
    }
    
    @FXML
    void mouseClicked(MouseEvent event) {
    	if(Math.abs(mousePoint.getX() - event.getX()) > 5 || Math.abs(mousePoint.getY() - event.getY()) > 5) return;
    	testSelection = event.isShiftDown() ? "next" : (isTesting ? "" : "init");
    	isTesting = !isTesting || event.isShiftDown() || event.isMetaDown() || event.isAltDown() || event.isControlDown();
    	testMode = event.isMetaDown() ? "solution" : (event.isControlDown() ? "computed" : "diff");
    	processImages();
    }
    
 	@FXML
    void zoomChanged() {
    	double zoomFactor = zoomSlider.getValue();
		zoomLabel.setText(String.format("%.1f", zoomFactor));
    	zoom(originalImageView, originalScrollPane, zoomFactor);
    	zoom(filteredImageView, filteredScrollPane, zoomFactor);
    }
    
    @FXML
    void reset() {
    	zoomSlider.setValue(1);
    	kernelSizeSlider.setValue(1);
    	sigmaSlider.setValue(1);
    	zoomChanged();
    	processImages();
    }
    
	@FXML
	public void initialize() {
		// load and process default image
		origImg = new RasterImage(new File(initialFileName));
		origImg.setToView(originalImageView);
		processImages();
	}
	
	@FXML
	private void processImages() {
		sigma = sigmaSlider.getValue();
		sigmaLabel.setText(String.format("%.1f", sigma));
		kernelSize = (int)kernelSizeSlider.getValue() | 1; // ensure odd integer value
    	kernelSizeLabel.setText(kernelSize + " x " + kernelSize);

		if(originalImageView.getImage() == null)
			return; // no image: nothing to do
		
		long startTime = System.currentTimeMillis();
		
		RasterImage srcImg = new RasterImage(origImg);
		srcImg.convertToGray();
		srcImg.setToView(originalImageView);
		
		RasterImage filteredImg = new RasterImage(srcImg.width, srcImg.height);
		
		GaussFilter filter = new GaussFilter();
		filter.apply(srcImg, filteredImg, kernelSize, sigma);
		kernel = filter.getKernel();
		showKernelInfo(kernel);
		
		filteredImg.setToView(filteredImageView);
		
	   	messageLabel.setText("Processing time: " + (System.currentTimeMillis() - startTime) + " ms");
	   	
	   	if(isTesting)
	   		isTesting = test();
	   	else
	   		messageLabel.setEffect(null);
	}
	
    
 	private void showKernelInfo(double[][] kernel) {
		GraphicsContext gc = kernelInfoCanvas.getGraphicsContext2D();
		double width = kernelInfoCanvas.getWidth();
		double height = kernelInfoCanvas.getHeight();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);
		
		if(kernel == null) {
 			kernelInfoTextArea.setText("No kernel yet...");
 			return;
 		}
		for(int ky = 0; ky < kernel.length; ky++) {
			if(kernel[ky] == null || kernel[ky].length != kernel[0].length) {
				kernelInfoTextArea.setText("Invalid 2D kernel array.");
				return;
			}
		}
 		String info = String.format("%d x %d Coefficients:\n\n", kernel[0].length, kernel.length);
 		double sum = 0;
		for(int ky = 0; ky < kernel.length; ky++) {
			for(int kx = 0; kx < kernel[0].length; kx++) {
				double value =  kernel[ky][kx];
				info += String.format("%.3f \t", value);
				sum += value;
			}
			info += "\n";
		}
		info += String.format("\nSum = %.3f \t", sum);
		kernelInfoTextArea.setText(info);
		
		int offsetX = kernel[0].length / 2;
		int offsetY = kernel.length / 2;
		double[] values = kernel[offsetY];
		double max = 0;
		for(int i = 0; i < values.length; i++) {
			if(values[i] > max) max = values[i];
		}
		
		double border = 6;
		double baseY = height - 2 * border;
		double barWidth = 10;
		gc.setFill(Color.BLACK);
		gc.setFont(new Font(10));
		gc.setTextAlign(TextAlignment.CENTER);
		for(int i = 0; i < values.length; i++) {
			double x = width/2 + ((i - offsetX) * width) / kernelSizeSlider.getMax();
			if(max > 0) {
				double barHeight = values[i] * (height - 3 * border) / max;
				gc.fillRect(x - barWidth/2 + 0.5, baseY - barHeight + 0.5, barWidth, barHeight);
			}
			gc.fillText("" + (i - offsetX), x + 0.5, height - 2);
		}
 	}
 	
	private Method testMethod = null;
	private Object testObj = null;
	private boolean isTesting = false;
	private String testSelection = "";
	private String testMode = "";

 	private boolean test() {
        try {
        	if(testMethod == null) {
        		Class<?> testClass;
        		String className = "testing.bv1c.Test";
        		try {
        			String path = System.getProperty("user.home") + File.separator + "src" + File.separator + "Java" + File.separator + "KJ_Testing.jar";
        			URL url = new File(path).toURI().toURL();
        			URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        			Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        			addMethod.setAccessible(true);
        			addMethod.invoke(classLoader, url);
        			testClass = classLoader.loadClass(className);
        		} catch (Exception e) {
            		testClass = ClassLoader.getSystemClassLoader().loadClass(className);
         		}
        		Constructor<?> constructor = testClass.getConstructor();
        		testObj = constructor.newInstance();
        		testMethod = testClass.getMethod("test", Object.class, String.class, String.class);
        	}
    		testMethod.invoke(testObj, this, testSelection, testMode);
    		testSelection = "";
    		return true;
		} catch (Exception e) {
			if(testMethod != null) e.printStackTrace();
	        messageLabel.setText("No test available");
    		return false;
	    }

 	}
 	
	private void zoom(ImageView imageView, ScrollPane scrollPane, double zoomFactor) {
		if(zoomFactor == 1) {
			scrollPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
			scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
			imageView.setFitWidth(0);
			imageView.setFitHeight(0);
		} else {
			double paneWidth = scrollPane.getWidth();
			double paneHeight = scrollPane.getHeight();
			double imgWidth = imageView.getImage().getWidth();
			double imgHeight = imageView.getImage().getHeight();
			double lastZoomFactor = imageView.getFitWidth() <= 0 ? 1 : imageView.getFitWidth() / imgWidth;
			if(scrollPane.getPrefWidth() == Region.USE_COMPUTED_SIZE)
				scrollPane.setPrefWidth(paneWidth);
			if(scrollPane.getPrefHeight() == Region.USE_COMPUTED_SIZE)
				scrollPane.setPrefHeight(paneHeight);
			double scrollX = scrollPane.getHvalue();
			double scrollY = scrollPane.getVvalue();
			double scrollXPix = ((imgWidth * lastZoomFactor - paneWidth) * scrollX + paneWidth/2) / lastZoomFactor;
			double scrollYPix = ((imgHeight * lastZoomFactor - paneHeight) * scrollY + paneHeight/2) / lastZoomFactor;
			imageView.setFitWidth(imgWidth * zoomFactor);
			imageView.setFitHeight(imgHeight * zoomFactor);
			if(imgWidth * zoomFactor > paneWidth)
				scrollX = (scrollXPix * zoomFactor - paneWidth/2) / (imgWidth * zoomFactor - paneWidth);
			if(imgHeight * zoomFactor > paneHeight)
				scrollY = (scrollYPix * zoomFactor - paneHeight/2) / (imgHeight * zoomFactor - paneHeight);
			if(scrollX < 0) scrollX = 0;
			if(scrollX > 1) scrollX = 1;
			if(scrollY < 0) scrollY = 0;
			if(scrollY > 1) scrollY = 1;
			scrollPane.setHvalue(scrollX);
			scrollPane.setVvalue(scrollY);
		}
	}

}
 		   		    	 





