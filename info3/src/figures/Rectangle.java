package figures;

public class Rectangle extends Figure{
    private int width;
    private int height;
    private Point topLeft;

    @Override
    public double area()
    {
        return width*height;

    }

    //command N -> Generate -> Constructor -> alle Attribute auswählen (mit shift)
    public Rectangle(int width, int height, Point topLeft) {
        this.width = width;
        this.height = height;
        this.topLeft = topLeft;
    }
}
