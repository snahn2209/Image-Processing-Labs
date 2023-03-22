// BV Ue4 WS2022/23 Vorgabe
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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;

public class ImageAnalysisAppController {
 		   		    	 
	private static final String initialFileName = "mountains.png";
	private static File fileOpenPath = new File(".");
	
	public enum StatsProperty {
		Minimum, Maximum, Mean, Median, Variance, Entropy;
		
	    private final SimpleStringProperty name;
	    private final SimpleStringProperty value;
	    
	    private StatsProperty() {
	        this.name = new SimpleStringProperty(name());
	        this.value = new SimpleStringProperty("n/a");
	    }
 		   		    	 
	    public String getName() { return name.get(); }
	    public void setName(String name) { this.name.set(name); }
	    public String getValue() { return value.get(); }
	    public void setValue(Double value) { this.value.set((value == null) ? "n/a" : String.format("%.2f", value)); }
	    public void setValue(Integer value) { this.value.set((value == null) ? "n/a" : String.format("%d", value)); }
	    public void setDiffValue(Double v1, Double v2) { this.value.set(((v1 == null) ? "n/a" : String.format("%.2f", v1)) + ((v2 == null) ? " != n/a" : String.format(" != %.2f", v2))); }
	    public void setDiffValue(Integer v1, Integer v2) { this.value.set(((v1 == null) ? "n/a" : String.format("%d", v1)) + ((v2 == null) ? " != n/a" : String.format(" != %d", v2))); }
	}
	
	final ObservableList<StatsProperty> statsData = FXCollections.observableArrayList(StatsProperty.values());
	
	public enum Visualization { 
		NONE("No Visualization"),
		ENTROPY("Entropy"),
		VARIANCE("Variance");
		
		private final String name;       
	    private Visualization(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
	private RasterImage originalImage;
	private ToneCurve toneCurve;
	private Histogram histogram;
	private Histogram toneCurveHistogram;
	
    private GraphicsContext selectionGC;
    private GraphicsContext histogramGC;
    private GraphicsContext toneCurveHistogramGC;
    private GraphicsContext toneCurveGC;
	
    private Rectangle selectionRect;
	private Point2D selectionStartPoint;
	private double zoom = 1;
	
    @FXML
    private BorderPane rootPane;

    @FXML
    private Slider minSlider;
 		   		    	 
    @FXML
    private Label minLabel;

    @FXML
    private Slider maxSlider;

    @FXML
    private Label maxLabel;

    @FXML
    private Slider minOutputSlider;

    @FXML
    private Label minOutputLabel;

    @FXML
    private Slider maxOutputSlider;

    @FXML
    private Label maxOutputLabel;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Slider histogramZoomSlider;

    @FXML
    private Slider thresholdSlider;

    @FXML
    private Label thresholdLabel;

    @FXML
    private Label thresholdTitleLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private ImageView overlayImageView;

    @FXML
    private TitledPane imageTitledPane;

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private StackPane imageStackPane;

    @FXML
    private Canvas selectionCanvas;

    @FXML
    private Label selectionLabel;

    @FXML
    private Canvas histogramCanvas;

    @FXML
    private Canvas histogramCompareCanvas;

    @FXML
    private StackPane histogramStackPane;

    @FXML
    private TitledPane histogramTitledPane;

    @FXML
    private Canvas toneCurveCanvas;
 		   		    	 
    @FXML
    private Canvas toneCurveCompareCanvas;

    @FXML
    private Canvas toneCurveOverlayCanvas;

    @FXML
    private Canvas toneCurveHistogramCanvas;

    @FXML
    private TableView<StatsProperty> statsTableView;
    
    @FXML
    private TableColumn<StatsProperty, String> statsNamesColoumn;

    @FXML
    private TableColumn<StatsProperty, String> statsValuesColoumn;

    @FXML
    private ComboBox<Visualization> visualizationSelection;

    @FXML
    private Label messageLabel;

    private Label infoLabel = new Label("");

    @FXML
    void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath); 
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if(selectedFile != null) {
			fileOpenPath = selectedFile.getParentFile();
			originalImage = new RasterImage(selectedFile);
			initImage();
			resetToneCurve();
			messageLabel.getScene().getWindow().sizeToScene();
		}
    }
 		   		    	 
    @FXML
    void minChanged() {
    	turnOffVisualization();
    	int min = (int)minSlider.getValue();
    	int max = (int)maxSlider.getValue();
    	if(min > 254) {
    		min = 254;
    		minSlider.setValue(min);
    	}
    	if(max < min + 1) {
    		max = min + 1;
    		maxSlider.setValue(max);
    	}
    	processImage();
    }
    
    @FXML
    void maxChanged() {
    	turnOffVisualization();
    	int min = (int)minSlider.getValue();
    	int max = (int)maxSlider.getValue();
    	if(max < 1) {
    		max = 1;
    		maxSlider.setValue(max);
    	}
		if(min > max - 1) {
			min = max - 1;
			minSlider.setValue(min);
		}
    	processImage();
    }
    
    @FXML
    void minOutputChanged() {
    	turnOffVisualization();
    	int minOutput = (int)minOutputSlider.getValue();
    	int maxOutput = (int)maxOutputSlider.getValue();
    	if(maxOutput < minOutput) {
    		maxOutput = minOutput;
    		maxOutputSlider.setValue(maxOutput);
    	}
    	processImage();
    }
    
    @FXML
    void maxOutputChanged() {
    	turnOffVisualization();
    	int minOutput = (int)minOutputSlider.getValue();
    	int maxOutput = (int)maxOutputSlider.getValue();
		if(minOutput > maxOutput) {
			minOutput = maxOutput;
			minOutputSlider.setValue(minOutput);
		}
		processImage();
    }
    
    @FXML
    void thresholdChanged() {
    	processImage();
    }
    
    @FXML
    void visualizationChanged() {
    	boolean disable = false;
    	switch(visualizationSelection.getValue()) {
		case ENTROPY:
			thresholdSlider.setMax(8);
			thresholdSlider.setValue(5.2);
			break;
		case VARIANCE:
			thresholdSlider.setMax(2000);
			thresholdSlider.setValue(280);
			break;
		default:
			disable = true;
			break;
    	}
		thresholdSlider.setDisable(disable);
		thresholdLabel.setDisable(disable);
		thresholdTitleLabel.setDisable(disable);
    	processImage();
    }
 		   		    	 
    void turnOffVisualization() {
    	visualizationSelection.setValue(Visualization.NONE);
    }
    
    void reset() {
    	zoomSlider.setValue(1);
    	zoomChanged();
    	histogramZoomSlider.setValue(1);
    	histogramZoomChanged();
    	minSlider.setValue(0);
    	maxSlider.setValue(255);
    	minOutputSlider.setValue(0);
    	maxOutputSlider.setValue(255);
    	processImage();
    }
    
    @FXML
    void resetToneCurve() {
    	turnOffVisualization();
    	reset();
    }
    
    @FXML
    void selectionBegan(MouseEvent event) {
    	selectionStartPoint = new Point2D((event.getX() + canvasOffsetX) / zoom, (event.getY() + canvasOffsetY) / zoom);
    	selectionRect.setX(selectionStartPoint.getX());
    	selectionRect.setY(selectionStartPoint.getY());
    	selectionRect.setWidth(0);
    	selectionRect.setHeight(0);
    	processImage();
    }
    
    @FXML
    void selectionResized(MouseEvent event) {
    	double ex = (event.getX() + canvasOffsetX) / zoom;
    	double ey = (event.getY() + canvasOffsetY) / zoom;
    	if(ex < 0) ex = 0;
    	if(ey < 0) ey = 0;
    	if(ex > originalImage.width) ex = originalImage.width;
    	if(ey > originalImage.height) ey = originalImage.height;
    	double width = ex - selectionStartPoint.getX();
    	double height = ey - selectionStartPoint.getY();
    	// set normalized rectangle
    	if(width >= 0) {
    		selectionRect.setX(selectionStartPoint.getX());
    		selectionRect.setWidth(width);
    	} else {
    		selectionRect.setX(ex);
    		selectionRect.setWidth(-width);
    	}
    	if(height >= 0) {
    		selectionRect.setY(selectionStartPoint.getY());
    		selectionRect.setHeight(height);
    	} else {
    		selectionRect.setY(ey);
    		selectionRect.setHeight(-height);
    	}
    	processImage();
    }
    
    @FXML
    void selectionEnded(MouseEvent event) {
    	double width = (event.getX() + canvasOffsetX) / zoom - selectionStartPoint.getX();
    	double height = (event.getY() + canvasOffsetY) / zoom - selectionStartPoint.getY();
    	if(Math.abs(width) <= 1 && Math.abs(height) <= 1) {
    		resetSelection();
    	} else {
    		selectionResized(event);
    	}
    }
    
    @FXML
    void zoomChanged() {
    	zoom = zoomSlider.getValue();
    	zoom(imageView, overlayImageView, imageScrollPane);
    	drawSelection();
    }
 		   		    	 
    @FXML
    void histogramZoomChanged() {
    	double zoom = histogramZoomSlider.getValue();
    	int width = (int)histogramCanvas.getWidth();
    	int height = (int)histogramCanvas.getHeight();
    	histogramStackPane.setMinWidth(width * zoom);
    	histogramStackPane.setMinHeight(height * zoom);
    	histogramCanvas.setScaleX(zoom);
    	histogramCanvas.setScaleY(zoom);
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
    	if(testSelection == "next" || testSelection == "init") reset();
    	processImage();
    }
    
	@FXML
	public void initialize() {
		ChangeListener<Object> changeListener = new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				drawSelection();
			}
		};
		imageScrollPane.hvalueProperty().addListener(changeListener);
		imageScrollPane.vvalueProperty().addListener(changeListener);
		imageScrollPane.widthProperty().addListener(changeListener);
		imageScrollPane.heightProperty().addListener(changeListener);
		
		// initialize table view
		statsNamesColoumn.setCellValueFactory(new PropertyValueFactory<StatsProperty, String>("name"));
		statsValuesColoumn.setCellValueFactory(new PropertyValueFactory<StatsProperty, String>("value"));
		statsTableView.setItems(statsData);
		statsTableView.setSelectionModel(null);
		
		// initialize parameters
		visualizationSelection.getItems().addAll(Visualization.values());
		visualizationSelection.setValue(Visualization.NONE);
		thresholdSlider.setDisable(true);
		thresholdLabel.setDisable(true);
		thresholdTitleLabel.setDisable(true);
		BorderPane.setAlignment(infoLabel, Pos.CENTER_LEFT);
		infoLabel.setMinHeight(25);
		infoLabel.setAlignment(Pos.BOTTOM_LEFT);
		
		// load and process default image
		originalImage = new RasterImage(new File(initialFileName));
		initImage();
	}
	
	private void initImage() {
		originalImage.convertToGray();
		originalImage.setToView(imageView);
        selectionCanvas.setWidth(originalImage.width);
        selectionCanvas.setHeight(originalImage.height);
		overlayImageView.setFitWidth(originalImage.width);
		overlayImageView.setFitHeight(originalImage.height);
		toneCurveHistogramGC = null;
        resetSelection();
	}
	
	private void resetSelection() {
        selectionRect = new Rectangle(0, 0, originalImage.width, originalImage.height);
		processImage();
    }
 		   		    	 
	private double canvasOffsetX;
	private double canvasOffsetY;
    
	private void drawSelection() {
		double zoomedWidth = Math.ceil(originalImage.width * zoom);
		double zoomedHeight = Math.ceil(originalImage.height * zoom);
		double scrollX = imageScrollPane.getHvalue();
		double scrollY = imageScrollPane.getVvalue();
		double viewingWidth = Math.min(imageScrollPane.getWidth(), zoomedWidth);
		double viewingHeight = Math.min(imageScrollPane.getHeight(), zoomedHeight);
		canvasOffsetX = (zoomedWidth - viewingWidth) * scrollX;
		canvasOffsetY = (zoomedHeight - viewingHeight) * scrollY;
		selectionCanvas.setTranslateX(canvasOffsetX);
		selectionCanvas.setTranslateY(canvasOffsetY);
		selectionCanvas.setWidth(viewingWidth);
		selectionCanvas.setHeight(viewingHeight);
		selectionGC.setTransform(new Affine(new Translate(-canvasOffsetX, -canvasOffsetY)));
		
		selectionGC.clearRect(0, 0, originalImage.width * zoom, originalImage.height * zoom);
        
        // round to integer positions
        int x = (int)(selectionRect.getX() + 0.5);
        int y = (int)(selectionRect.getY() + 0.5);
        int w = (int)(selectionRect.getWidth() + 0.5);
        int h = (int)(selectionRect.getHeight() + 0.5);
        selectionRect.setX(x);
        selectionRect.setY(y);
        selectionRect.setWidth(w);
        selectionRect.setHeight(h);
        
        selectionGC.setStroke(Color.RED);
        selectionGC.setLineWidth(2);
        selectionGC.strokeRect(x * zoom, y * zoom, w * zoom, h * zoom);
		
        selectionLabel.setText(String.format("Selection (x=%d, y=%d, w=%d, h=%d)", x, y, w, h));
	}
		
	private void drawToneCurveOverlay() {
		GraphicsContext gc = toneCurveOverlayCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, toneCurveOverlayCanvas.getWidth(), toneCurveOverlayCanvas.getHeight());

		gc.setLineWidth(1);
		int min = (int)minSlider.getValue();

		int max = (int)maxSlider.getValue();
		int minOutput = (int)minOutputSlider.getValue();
		int maxOutput = (int)maxOutputSlider.getValue();
		double trans = 0.5;
		gc.setStroke(Color.gray(0.4));
		gc.setLineDashes(5.0, 5.0);
		gc.strokeLine(min + trans, trans, min + trans, 256);
		gc.strokeLine(max + trans, trans, max + trans, 256);
		gc.strokeLine(0, 255 - minOutput + trans, 256, 255 - minOutput + trans);
		gc.strokeLine(0, 255 - maxOutput + trans, 256, 255 - maxOutput + trans);
		gc.setStroke(Color.gray(0.8));
		gc.setLineDashes();		
	}

	
	private void processImage() {
    	int min = (int)minSlider.getValue();
    	minLabel.setText("" + min);
    	int max = (int)maxSlider.getValue();
    	maxLabel.setText("" + max);
    	int minOutput = (int)minOutputSlider.getValue();
    	minOutputLabel.setText("" + minOutput);
    	int maxOutput = (int)maxOutputSlider.getValue();
    	maxOutputLabel.setText("" + maxOutput);
    	double threshold = thresholdSlider.getValue();
    	thresholdLabel.setText(String.format("%.1f", threshold));

    	if(imageView.getImage() == null)
			return; // no image: nothing to do
		
		long startTime = System.currentTimeMillis();
 		   		    	 
        if (selectionGC == null) {
        	selectionGC = selectionCanvas.getGraphicsContext2D();
        }
        if (histogramGC == null) {
        	histogramGC = histogramCanvas.getGraphicsContext2D();
        	histogram = new Histogram(histogramGC, (int)histogramCanvas.getHeight());
        }
        if (toneCurveHistogramGC == null) {
        	toneCurveHistogramGC = toneCurveHistogramCanvas.getGraphicsContext2D();
        	toneCurveHistogram = new Histogram(toneCurveHistogramGC, (int)toneCurveHistogramCanvas.getHeight());
    		toneCurveHistogram.setImageRegion(originalImage, (int)selectionRect.getX(), (int)selectionRect.getY(), (int)selectionRect.getWidth(), (int)selectionRect.getHeight());
    		toneCurveHistogram.draw(Color.gray(0.6));
        }
        if (toneCurveGC == null) {
        	toneCurveGC = toneCurveCanvas.getGraphicsContext2D();
        	toneCurve = new ToneCurve(toneCurveGC);
        }
        
        toneCurve.updateTable(min, max, minOutput, maxOutput);
		RasterImage image = new RasterImage(originalImage);
		toneCurve.applyTo(image);
		image.setToView(imageView);
		
		histogram.setImageRegion(image, (int)selectionRect.getX(), (int)selectionRect.getY(), (int)selectionRect.getWidth(), (int)selectionRect.getHeight());
		
		statsData.get(StatsProperty.Minimum.ordinal()).setValue(histogram.getMinimum());
		statsData.get(StatsProperty.Maximum.ordinal()).setValue(histogram.getMaximum());
		statsData.get(StatsProperty.Mean.ordinal()).setValue(histogram.getMean());
		statsData.get(StatsProperty.Median.ordinal()).setValue(histogram.getMedian());
		statsData.get(StatsProperty.Variance.ordinal()).setValue(histogram.getVariance());
		statsData.get(StatsProperty.Entropy.ordinal()).setValue(histogram.getEntropy());
				
		drawSelection();
		toneCurve.draw(Color.BLUE);
		histogram.draw(Color.BLACK);
		statsTableView.refresh();
		drawToneCurveOverlay();

		RasterImage overlayImage = new RasterImage(image.width, image.height, 0);
		if(visualizationSelection.getValue() != Visualization.NONE) {
			RasterImage overlayResultImage = image.getOverlayImage(11, visualizationSelection.getValue(), threshold);
			if(overlayResultImage != null) {
				overlayImage = overlayResultImage;
			}
		}
		overlayImage.setToView(overlayImageView);

	   	messageLabel.setText("Processing time: " + (System.currentTimeMillis() - startTime) + " ms");
 		   		    	 
	   	if(isTesting) {
	   		isTesting = test();
	   	} else {
	   		histogramCompareCanvas.setOpacity(0);
	   		toneCurveCompareCanvas.setOpacity(0);
			histogramCanvas.setOpacity(1);
			toneCurveCanvas.setOpacity(1);
	   		statsTableView.setEffect(null);
	   		histogramTitledPane.setEffect(null);
	   		imageTitledPane.setEffect(null);
	   		if(rootPane.getBottom() != null) {
	   			rootPane.setBottom(null);
	   			rootPane.getScene().getWindow().sizeToScene();
	   		}
	   	}
	}
	
	private Method testMethod = null;
	private Object testObj = null;
	private boolean isTesting = false;
	private String testSelection = "";
	private String testMode = "";
	
	public void setStatValues(Number[] v1, Number[] v2, boolean[] diff, boolean[] isDouble) {
		for(int i = 0; i < v1.length; i++) {
			StatsProperty prop = statsData.get(i);
			if(isDouble[i]) {
				if(diff[i]) prop.setDiffValue((Double)v1[i], (Double)v2[i]); else prop.setValue((Double)v1[i]);
			} else {
				if(diff[i]) prop.setDiffValue((Integer)v1[i], (Integer)v2[i]); else prop.setValue((Integer)v1[i]);
			}
		}
		statsTableView.refresh();
	}
	
	public void setVisualization(Integer ordinal) {
		visualizationSelection.setValue(Visualization.values()[ordinal]);
		visualizationChanged();
	}
	
	public int getVisualizationOrdinal() {
		return visualizationSelection.getValue().ordinal();
	}
 		   		    	 
 	private boolean test() {
        try {
        	if(testMethod == null) {
        		Class<?> testClass;
        		String className = "testing.bv5d.Test";
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
        		testMethod = testClass.getMethod("test", Object.class, String.class, String.class, Image.class, int[].class, Number[].class);
        	}
        	Number[] statsValues = { histogram.getMinimum(), histogram.getMaximum(), histogram.getMean(), histogram.getMedian(), histogram.getVariance(), histogram.getEntropy()};
        	
	   		if(rootPane.getBottom() == null) {
	   			rootPane.setBottom(infoLabel);
	   			rootPane.getScene().getWindow().sizeToScene();
	   		}
    		testMethod.invoke(testObj, this, testSelection, testMode, originalImage.getImage(), histogram.getValues(), statsValues);
    		testSelection = "";
    		return true;
		} catch (Exception e) {
			if(testMethod != null) e.printStackTrace();
	        messageLabel.setText("No test available");
	        return false;
	    }

 	}
 	
	private void zoom(ImageView imageView, ImageView overlayImageView, ScrollPane scrollPane) {
		if(zoom == 1) {
			scrollPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
			scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
			imageView.setFitWidth(0);
			imageView.setFitHeight(0);
			overlayImageView.setFitWidth(originalImage.width);
			overlayImageView.setFitHeight(originalImage.height);
		} else {
			double paneWidth = scrollPane.getWidth();
			double paneHeight = scrollPane.getHeight();
			if(scrollPane.getPrefWidth() == Region.USE_COMPUTED_SIZE)
				scrollPane.setPrefWidth(paneWidth);
			if(scrollPane.getPrefHeight() == Region.USE_COMPUTED_SIZE)
				scrollPane.setPrefHeight(paneHeight);
			imageView.setFitWidth(originalImage.width * zoom);
			imageView.setFitHeight(originalImage.height * zoom);
			overlayImageView.setFitWidth(originalImage.width * zoom);
			overlayImageView.setFitHeight(originalImage.height * zoom);
		}
	}

	
 		   		    	 


}
 		   		    	 




