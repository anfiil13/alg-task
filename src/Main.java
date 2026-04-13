public class Main {
    public static void main(String[] args) {

        Shape circle = new Circle(5);
        Shape triangle = new Triangle(3, 4, 5);
        Shape rectangle = new Rectangle(4, 6);

        printShape(circle);
        printShape(triangle);
        printShape(rectangle);
    }

    public static void printShape(Shape shape) {
        System.out.println("Type: " + shape.getType());
        System.out.println("Area: " + shape.getArea());
        System.out.println("Perimeter: " + shape.getPerimeter());
        System.out.println("-------------------");
    }
}