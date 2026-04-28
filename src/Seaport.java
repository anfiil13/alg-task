public class Seaport {

    static class Port {
        private final int maxBerths;
        private int occupied = 0;

        public Port(int maxBerths) {
            this.maxBerths = maxBerths;
        }

        public synchronized void dock(int shipId) throws InterruptedException {
            while (occupied >= maxBerths) {
                wait();
            }
            occupied++;
            System.out.println("Судно " + shipId + " → разгрузка");
        }

        public synchronized void undock(int shipId) {
            occupied--;
            System.out.println("Судно " + shipId + " ← ушло");
            notifyAll();
        }
    }

    static class Ship implements Runnable {
        private final int id;
        private final Port port;

        Ship(int id, Port port) { this.id = id; this.port = port; }

        public void run() {
            try {
                port.dock(id);
                Thread.sleep(500);
                port.undock(id);
            } catch (InterruptedException e) {}
        }
    }

    public static void main(String[] args) {
        Port port = new Port(3);
        for (int i = 1; i <= 8; i++) {
            new Thread(new Ship(i, port)).start();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
    }
}