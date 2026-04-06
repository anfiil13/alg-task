// Создать очередь, информационными полями которой являются: фамилия
//
//11 и средний бал студента. Добавить в очередь сведения о новом студенте.
//
//Организовать просмотр данных очереди.
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int SIZE = 10;
        String[][][] queue = new String[SIZE][1][2];

        int front = 0;
        int rear = 0;

        while (true) {
            System.out.println("\n1 - Добавить студента");
            System.out.println("2 - Просмотреть очередь");
            System.out.println("3 - Выход");
            System.out.print("Выберите действие: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    if (rear < SIZE) {
                        System.out.print("Введите фамилию: ");
                        String surname = scanner.nextLine();

                        System.out.print("Введите средний балл: ");
                        String average = scanner.nextLine();

                        queue[rear][0][0] = surname;
                        queue[rear][0][1] = average;

                        rear++;
                        System.out.println("Студент добавлен в очередь.");
                    } else {
                        System.out.println("Очередь переполнена!");
                    }
                    break;

                case 2:
                    if (front == rear) {
                        System.out.println("Очередь пуста.");
                    } else {
                        System.out.println("\nСодержимое очереди:");
                        for (int i = front; i < rear; i++) {
                            System.out.println(
                                    "Фамилия: " + queue[i][0][0] +
                                            ", Средний балл: " + queue[i][0][1]
                            );
                        }
                    }
                    break;

                case 3:
                    System.out.println("Выход из программы.");
                    return;

                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }
}

