// максимальное значение в стэке
import java.util.Scanner;
import java.util.Stack;
public class Main1 {
   public static void main(String[] args) {
   Stack<Double> stack = new Stack<>();
   Scanner scanner = new Scanner(System.in);
   System.out.println("Введите размер массива:");
   int size = scanner.nextInt();
   System.out.println("Введите " + size + " чисел ");
   for (int i = 0; i < size; i++ ){
       double value = scanner.nextInt();
       stack.push(value);
   }
   System.out.println("Стек: " + stack);
   double max = Double.MIN_VALUE;
   for(double num : stack){
       if (num > max) {
           max = num;
       }
   }
       System.out.println("Максимум: " + max);
   }
}
