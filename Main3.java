// минимальный элемент очереди
import java.util.Scanner;
import java.util.Queue;
import java.util.LinkedList;

public class Main3 {
    public static void main(String[] args) {
        Queue<Double> queue = new LinkedList<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите количество элементов очереди:");

        int size = scanner.nextInt();
        System.out.println("Введите " + size + " чисел(ла) ");

        for (int i = 0; i < size; i++) {
            Double value = scanner.nextDouble();
            queue.add(value);
        }

        System.out.println("Очередь: " + queue);
        double min = Double.MAX_VALUE;
        for(double num : queue){
            if (num < min) {
                min = num;
            }
        }
        System.out.println("Минимум: " + min);
    }
}
