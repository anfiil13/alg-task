public class Main {
    public static void main(String[] args) {

        Tree tree = new Tree();

        tree.add(5);
        tree.add(2);
        tree.add(8);
        tree.add(1);

        System.out.println("Элементы:");
        for (Integer i : tree) {
            System.out.println(i);
        }

        tree.remove(2);

        System.out.println("После удаления:");
        for (Integer i : tree) {
            System.out.println(i);
        }
    }
}