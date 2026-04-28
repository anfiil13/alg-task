public class Main {
    public static void main(String[] args) {

        Tree<Integer> intTree = new Tree<>();
        
        intTree.add(5);
        intTree.add(2);
        intTree.add(8);
        intTree.add(1);

        System.out.println("Элементы:");
        for (Integer i : intTree) {
            System.out.println(i);
        }

        intTree.remove(2);
        System.out.println("После удаления:");
        for (Integer i : intTree) {
            System.out.println(i);
        }
        
        Tree<String> stringTree = new Tree<>();
        
        stringTree.add("яблоко");
        stringTree.add("банан");
        stringTree.add("апельсин");

        System.out.println("\nString дерево:");
        for (String s : stringTree) {
            System.out.println(s);
        }

        Tree<Double> doubleTree = new Tree<>();
        
        doubleTree.add(3.14);
        doubleTree.add(1.5);
        doubleTree.add(2.7);

        System.out.println("\nDouble дерево:");
        for (Double d : doubleTree) {
            System.out.println(d);
        } 
    }
}
