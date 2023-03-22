package figures;

public class Square extends Figure{
    private int length;

    public double area(int length){
        return length*length;
    }

    public Square(int length) {
        this.length = length;
    }

    public double area(){
        return length*length;
    }
}
