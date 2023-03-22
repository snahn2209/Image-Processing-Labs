// BV Ue3 WS2022/23 Vorgabe
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
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class MorphologicFilterAppController {
 		   		    	 
	private static final String initialFileName = "rhino_part.png";
	private static File fileOpenPath = new File(".");
	
	private static final MorphologicFilter filter = new MorphologicFilter();
	private int threshold;
	private boolean kernel[][]; // first dimension: y (row), second dimension: x (column)
	
	public enum KernelPreset { 
		CUSTOM("Custom"),
		RADIUS("Radius");
		
		private final String name;       
	    private KernelPreset(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
	public enum FilterType { 
		DILATION("Dilation"),
		EROSION("Erosion"),
		OPENING("Opening"),
		CLOSING("Closing");
		
		private final String name;       
	    private FilterType(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
    @FXML
    private Slider thresholdSlider;
 		   		    	 
    @FXML
    private Label thresholdLabel;

    @FXML
    private ComboBox<FilterType> filterSelection;

    @FXML
    private ComboBox<KernelPreset> kernelPresetSelection;

    @FXML
    private GridPane kernelGrid;
    
    @FXML
    private Slider kernelSlider;

    @FXML
    private Label radiusLabel;

    @FXML
    private ImageView originalImageView;

    @FXML
    private ScrollPane originalScrollPane;

    @FXML
    private ImageView binaryImageView;

    @FXML
    private ScrollPane binaryScrollPane;

    @FXML
    private ImageView filteredImageView;

    @FXML
    private ScrollPane filteredScrollPane;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label zoomLabel;

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
			new RasterImage(selectedFile).setToView(originalImageView);
	    	processImages();
	    	messageLabel.getScene().getWindow().sizeToScene();;
		}
    }
 		   		    	 
    @FXML
    void zoomChanged() {
    	double zoomFactor = zoomSlider.getValue();
		zoomLabel.setText(String.format("%.1f", zoomFactor));
    	zoom(originalImageView, originalScrollPane, zoomFactor);
    	zoom(binaryImageView, binaryScrollPane, zoomFactor);
    	zoom(filteredImageView, filteredScrollPane, zoomFactor);
    }
    
    @FXML
    void thresholdChanged() {
    	processImages();
    }
    
    @FXML
    void filterChanged() {
    	processImages();
    }
    
    @FXML
    void kernelChanged() {
    	kernelPresetSelection.setValue(KernelPreset.CUSTOM);
    	kernelSlider.setVisible(false);
    	radiusLabel.setVisible(false);
    	int numRows = kernelGrid.getRowConstraints().size();
    	int numColumns = kernelGrid.getColumnConstraints().size();
    	kernel = new boolean[numRows][numColumns];
    	for(Node child : kernelGrid.getChildren()) {
    	    Integer column = GridPane.getColumnIndex(child);
    	    if(column == null) column = 0;
    	    Integer row = GridPane.getRowIndex(child);
    	    if(row == null) row = 0;
    	    if(column < numColumns && row < numRows) {
    	        kernel[row][column] = ((CheckBox)child).isSelected();
    	    }
    	}    	
    	processImages();
    }
    
    @FXML
    void kernelPresetChanged() {
    	boolean showRadius = kernelPresetSelection.getValue() == KernelPreset.RADIUS;
    	kernelSlider.setVisible(showRadius);
    	radiusLabel.setVisible(showRadius);
    	if(!showRadius) return;
    	double radius = kernelSlider.getValue();
    	radiusLabel.setText(String.format("%.1f", radius));
    	double radiusSquared = radius * radius;
       	int numRows = kernelGrid.getRowConstraints().size();
    	int numColumns = kernelGrid.getColumnConstraints().size();
    	kernel = new boolean[numRows][numColumns];
    	for(Node child : kernelGrid.getChildren()) {
    	    Integer column = GridPane.getColumnIndex(child);
    	    if(column == null) column = 0;
    	    Integer row = GridPane.getRowIndex(child);
    	    if(row == null) row = 0;
    	    if(column < numColumns && row < numRows) {
    	    	double distSquared = Math.pow(row - numRows/2, 2) + Math.pow(column - numColumns/2, 2);
    	    	kernel[row][column] = distSquared <= radiusSquared;
    	    	((CheckBox)child).setSelected(kernel[row][column]);
    	    }
    	}    	
    	processImages();
    }
    
    void setKernel(boolean[][] kernel) {
    	kernelPresetSelection.setValue(KernelPreset.CUSTOM);
       	int numRows = kernelGrid.getRowConstraints().size();
    	int numColumns = kernelGrid.getColumnConstraints().size();
    	for(Node child : kernelGrid.getChildren()) {
    	    Integer column = GridPane.getColumnIndex(child);
    	    if(column == null) column = 0;
    	    Integer row = GridPane.getRowIndex(child);
    	    if(row == null) row = 0;
    	    if(column < numColumns && row < numRows) {
    	    	this.kernel[row][column] = kernel[row][column];
    	    	((CheckBox)child).setSelected(kernel[row][column]);
    	    }
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
	public void initialize() {
		// set combo boxes items
		filterSelection.getItems().addAll(FilterType.values());
		filterSelection.setValue(FilterType.DILATION);
		kernelPresetSelection.getItems().addAll(KernelPreset.values());
		kernelPresetSelection.setValue(KernelPreset.RADIUS);
		kernelSlider.setValue(1);
		
		// initialize parameters
		kernelPresetChanged();
		
		// load and process default image
		new RasterImage(new File(initialFileName)).setToView(originalImageView);
		processImages();
	}
	
	private void processImages() {
    	threshold = (int)thresholdSlider.getValue();
    	thresholdLabel.setText("" + threshold);

    	if(originalImageView.getImage() == null)
			return; // no image: nothing to do
		
		long startTime = System.currentTimeMillis();
		
		RasterImage origImg = new RasterImage(originalImageView); 
		RasterImage binaryImg = new RasterImage(origImg.width, origImg.height); 
		RasterImage filteredImg = new RasterImage(origImg.width, origImg.height); 
		
		filter.copy(origImg, binaryImg);
		binaryImg.binarize(threshold);
		
		switch(filterSelection.getValue()) {
		case DILATION:
			filter.dilation(binaryImg, filteredImg, kernel);
			break;
		case EROSION:
			filter.erosion(binaryImg, filteredImg, kernel);
			break;
		case OPENING:
			filter.opening(binaryImg, filteredImg, kernel);
			break;
		case CLOSING:
			filter.closing(binaryImg, filteredImg, kernel);
			break;
		default:
			break;
		}
		
		binaryImg.setToView(binaryImageView);
		filteredImg.setToView(filteredImageView);
		
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
        		String className = "testing.bv3c.Test";
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
        		testMethod = testClass.getMethod("test", Object.class, String.class, String.class, Integer.class, String.class, String.class, String.class, String.class, String.class);
        	}
        	String image1Name = "originalImageView";
        	String image2Name = "binaryImageView";
        	String image3View = "filteredImageView";
        	String slider1Name = "thresholdSlider";
        	String ComboBox1Name = "filterSelection";
    		testMethod.invoke(testObj, this, testSelection, testMode, filterSelection.getValue().ordinal(), image1Name, image2Name, image3View, slider1Name, ComboBox1Name);
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
 		   		    	 




