package figures;

import java.util.ArrayList;
import java.util.List;

public class Graphic {
    private List<Figure> figures = new ArrayList<>();

    public void addFigure (Figure f){
        this.figures.add(f);

    }
    public double totalArea() {
        double s= 0;
        for (Figure f : figures){
            s=s+f.area();
        }
        return s;
    }

     public static void main(String[] args){
        Point point = new Point(42,42);
        Rectangle rectangle = new Rectangle (10,20,point);
        Figure figure = new Rectangle (30,30,point);
        Graphic graphic = new Graphic();
        graphic.addFigure(rectangle);
        graphic.addFigure(figure);
     }
}
