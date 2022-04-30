import java.util.Scanner;

public class Application implements Runnable {
    private Wallet wallet = new Wallet();
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(true){
            switch (scanner.nextLine()){
                case "create":
                    create();
                    break;
                case "logIn":
                    logIn();
                    break;
                case "logOut":
                    return;
                case "balance":
                    balance();
                    break;
                case "history":
                    history();
                    break;
                case "transfer":
                    transfer();
                    break;
            }
        }
    }

    private void transfer() {
        Scanner scanner = new Scanner(System.in);
        String recipientAddress,amount;
        System.out.println("write recipient Address:");
        recipientAddress = scanner.next();
        System.out.println("write amount:");
        amount = scanner.next();
        wallet.transfer(recipientAddress,amount);
    }

    private void history() {
        wallet.history();
    }

    private void balance() {
        System.out.println(wallet.getBalance());
    }

    private void logIn() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("write password:");
        String password = scanner.next();
        boolean result = wallet.loadWallet(password);
        if (!result){
            System.out.println("there is no wallet");
        }
    }

    private void create() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("write password:");
        String password = scanner.next();
        wallet.generateWallet(password);
    }
}
