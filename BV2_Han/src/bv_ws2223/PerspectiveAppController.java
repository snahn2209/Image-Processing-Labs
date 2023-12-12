// BV Ue2 WS2022/23 Vorgabe
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

import bv_ws2223.GeometricTransform.InterpolationType;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class PerspectiveAppController {
 		   		    	 	
	private static final String initialFileName = "59009_512.jpg";
	private static File fileOpenPath = new File(".");
	private static final double angleStepSize = 5.0;	// used for angle increment and decrement
	private static final double scaleX = 1.35;
	private static final double scaleY = 1.05;
 		   		    	 
	private static final GeometricTransform transform = new GeometricTransform();
	private double angle;
	private double distortion;

    @FXML
    private Slider angleSlider;

    @FXML
    private Label angleLabel;

    @FXML
    private Slider distortionSlider;

    @FXML
    private Label distortionLabel;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label zoomLabel;

    @FXML
    private ComboBox<InterpolationType> interpolationSelection;

    @FXML
    private ImageView originalImageView;

    @FXML
    private ScrollPane originalScrollPane;

    @FXML
    private ImageView transformedImageView;

    @FXML
    private ScrollPane transformedScrollPane;

    @FXML
    private Label messageLabel;

    @FXML
    void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath); 
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if(selectedFile != null) {
			fileOpenPath = selectedFile.getParentFile();
			RasterImage img = new RasterImage(selectedFile);
			img.setToView(originalImageView);
	    	processImages();
	    	messageLabel.getScene().getWindow().sizeToScene();;
		}
    }
	
    @FXML
    void zoomChanged() {
    	double zoomFactor = zoomSlider.getValue();
		zoomLabel.setText(String.format("%.1f", zoomFactor));
    	zoom(transformedImageView, transformedScrollPane, zoomFactor);
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
    void reset() {
    	zoomSlider.setValue(1);
    	angleSlider.setValue(0);
    	distortionSlider.setValue(distortionSlider.getMin());
    	zoomChanged();
    	processImages();
    }
    
    @FXML 
    void speedTest() {
		RasterImage origImg = new RasterImage(originalImageView); 
		// enlarge view to get some space for rotation
		RasterImage transformedImg = new RasterImage((int)(origImg.width * scaleX), (int)(origImg.height * scaleY));
		
		InterpolationType type = interpolationSelection.getValue();
		double distortion = 0.001;
		
		long startTime = System.currentTimeMillis();
		
		int cnt = 0;
		for(int angle = 0; angle < 360; angle += angleStepSize) {
			transform.perspective(origImg, transformedImg, angle, distortion, type);
			cnt++;
		}
		
		long time = System.currentTimeMillis() - startTime;
		messageLabel.setText("Speed Test: Calculated " + cnt + " perspectives in " + time + " ms");
    }
    
	@FXML
	public void initialize() {
		// set combo boxes items
		interpolationSelection.getItems().addAll(InterpolationType.values());
		interpolationSelection.setValue(InterpolationType.NEAREST);
				
		// load and process default image
		RasterImage img = new RasterImage(new File(initialFileName));
		img.setToView(originalImageView);
		processImages();
	}
	
	@FXML
	private void processImages() {
    	angle = angleSlider.getValue();
    	angleLabel.setText(String.format("%.0f°", angle));
    	distortion = distortionSlider.getValue();
    	distortionLabel.setText(String.format("%.4f", distortion));
    	
		if(originalImageView.getImage() == null)
			return; // no image: nothing to do
		
		long startTime = System.currentTimeMillis();
		
		RasterImage origImg = new RasterImage(originalImageView); 
		// enlarge view to get some space for rotation
		RasterImage transformedImg = new RasterImage((int)(origImg.width * scaleX), (int)(origImg.height * scaleY));
		
		transform.perspective(origImg, transformedImg, angle, distortion, interpolationSelection.getValue());
		
		transformedImg.setToView(transformedImageView);
		
	   	messageLabel.setText("Processing time: " + (System.currentTimeMillis() - startTime) + " ms");
	   	
	   	if(isTesting)
	   		isTesting = test();
	   	else
	   		messageLabel.setEffect(null);
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
        		String className = "testing.bv2b.Test";
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
        		testMethod = testClass.getMethod("test", Object.class, String.class, String.class, Integer.class);
        	}
        	int typeIndex = Math.min(interpolationSelection.getValue().ordinal(), 1);
    		testMethod.invoke(testObj, this, testSelection, testMode, typeIndex);
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
 		   		    	 




