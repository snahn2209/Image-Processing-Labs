package figures;

public class Point extends Figure{
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public double area(){
        return 0;
    }
}
