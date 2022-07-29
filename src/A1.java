public class A1 {
    public static void main (String[] args) {
        A1 a = new A1();
        if (a.validateArgs(args)) {
            a.run(args);
        }
    }

    public boolean validateArgs(String[] args) {
        return true;
    }

    public void run(String[] args) {
        System.out.println("Hello");
    }
}
