package figures;

public class Circle extends Figure{
    private double radius;
    private Point center;

    @Override
    public double area(){
        return Math.PI * radius * radius;
    }

    public Circle(double radius, Point center) {
        this.radius = radius;
        this.center = center;
    }
    @Override
    public boolean equals(Object o){
        //return this.radius == o.radius; -> falsch weil bei der KLasse Object gibt es keinen radius
        if(!(o instanceof Circle)){
            return false;
        }
        Circle c= (Circle) o;
        return this.radius== c.radius && this.center.equals(c.center);
        // oder this.radius == ((Circle)o).radius; -> macht das selbe wie die beiden zeilen drüber (ohne &&...) -> Object casten
    }

    /*@Override
    public boolean equals(Object o){
        if(this==o) return true;
        if((o ==0) || getClass() != o.getClass()) return false;

    }*/

    public static void main(String[] args) {
        Point p1 = new Point (10,10);
        Circle c1=new Circle (5,p1);
        String s = "hello";
        c1.equals(s);
    }
}
