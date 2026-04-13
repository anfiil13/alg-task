public abstract class Shape {

    protected ShapeType type;

    public Shape(ShapeType type) {
        this.type = type;
    }

    public ShapeType getType() {
        return type;
    }

    public abstract double getArea();

    public abstract double getPerimeter();
}