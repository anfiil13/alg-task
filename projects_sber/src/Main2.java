// произведение нечётных значений
import java.util.Scanner;
import java.util.Stack;

public class Main2 {
    public static void main(String[] args) {

        Stack<Integer> stack = new Stack<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите размер массива:");

        int size = scanner.nextInt();
        System.out.println("Введите " + size + " чисел(ла) ");

        for (int i = 0; i < size; i++) {
            int value = scanner.nextInt();
            stack.push(value);
        }

        System.out.println("Стек: " + stack);
        int p = 1;

        for (Integer num : stack) {
            if (num % 2 != 0){
                p *= num;
            }
        }
        System.out.println("произведение нечётных значений: " + p);

    }

}
