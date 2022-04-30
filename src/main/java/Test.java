public class Test {
    public static void main(String[] args) {
        Wallet wallet = new Wallet();

        System.out.println("Generate Wallet:");
        wallet.generateWallet("1234");
        System.out.println("\n");
        wallet.generateWallet("12");

        System.out.println("\n\nLoad Wallet");
        wallet.loadWallet("1234");

        System.out.println("\n\nTranser");
        wallet.transfer("c7f0819022f508320537ea361bc15da8f6b70de175dd3619ad2be7bbc5875376",
                "0x14EC32975136C45bf3cAB41150D99A1ebC0B82B4","10");

        System.out.println("\n\nBalance");
        System.out.println(wallet.getBalance("0x14EC32975136C45bf3cAB41150D99A1ebC0B82B4"));

        System.out.println("\n\nHistory");
        wallet.history();
    }
}
