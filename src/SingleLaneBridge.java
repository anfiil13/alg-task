import java.util.concurrent.ThreadLocalRandom;

public class SingleLaneBridge {
    enum Direction { NORTH, SOUTH, NONE }

    static class Bridge {
        private Direction currentDirection = Direction.NONE;
        private int carsOnBridge = 0;

        public synchronized void enterNorth() throws InterruptedException {
            while (currentDirection == Direction.SOUTH) {
                wait();
            }
            currentDirection = Direction.NORTH;
            carsOnBridge++;
        }

        public synchronized void enterSouth() throws InterruptedException {
            while (currentDirection == Direction.NORTH) {
                wait();
            }
            currentDirection = Direction.SOUTH;
            carsOnBridge++;
        }

        public synchronized void exitNorth() {
            carsOnBridge--;
            if (carsOnBridge == 0) {
                currentDirection = Direction.NONE;
                notifyAll();
            }
        }

        public synchronized void exitSouth() {
            carsOnBridge--;
            if (carsOnBridge == 0) {
                currentDirection = Direction.NONE;
                notifyAll();
            }
        }
    }

    static class Car implements Runnable {
        private final Bridge bridge;
        private final Direction direction;
        private final int id;

        public Car(Bridge bridge, Direction direction, int id) {
            this.bridge = bridge;
            this.direction = direction;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                if (direction == Direction.NORTH) {
                    bridge.enterNorth();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                    bridge.exitNorth();
                    System.out.println("Машина " + id + " С->Ю");
                } else {
                    bridge.enterSouth();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                    bridge.exitSouth();
                    System.out.println("Машина " + id + " Ю->С");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Bridge bridge = new Bridge();

        for (int i = 1; i <= 10; i++) {
            Direction dir = ThreadLocalRandom.current().nextBoolean() ? Direction.NORTH : Direction.SOUTH;
            Thread carThread = new Thread(new Car(bridge, dir, i));
            carThread.start();
        }
    }
}