import com.google.gson.Gson;
import io.reactivex.Flowable;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import org.web3j.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class Wallet {
    private Constant constant = Constant.getInstance();
    private String privateKey = null,walletAddress = null;

    public Wallet(){}

    /**
     * create wallet
     * @param walletPassword
     */
    public void generateWallet(String walletPassword){
        Bip39Wallet wallet = null;
        try {
            wallet = WalletUtils.generateBip39Wallet(walletPassword,
                    new File(constant.getWalletDirectory()));
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Credentials credentials = WalletUtils.loadBip39Credentials(walletPassword, wallet.getMnemonic());
        this.privateKey = credentials.getEcKeyPair().getPrivateKey().toString();
        this.walletAddress = credentials.getAddress();
        System.out.println("password: " + walletPassword);
        System.out.println("private key: "+ privateKey);
        System.out.println("adress: " + credentials.getAddress());
        System.out.println("mnemonic: " + wallet.getMnemonic());
    }

    /**
     * logIn wallet
     * @param walletPassword
     * @param seedPhrase refers Mneminic Code
     */
    public void restoreWallet(String walletPassword, String seedPhrase){
        Credentials restoreCredentials = WalletUtils.loadBip39Credentials(walletPassword,
                seedPhrase);
        ECKeyPair restoredPrivateKey = restoreCredentials.getEcKeyPair();
        this.privateKey = restoreCredentials.getEcKeyPair().getPrivateKey().toString();
        this.walletAddress = restoreCredentials.getAddress();
        System.out.println(privateKey);
    }

    public boolean loadWallet(String pass){
        File folder = new File(constant.getWalletDirectory());
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory() && fileEntry.getName().contains("UTC")) {
                Credentials restoreCredentials;
                try {
                    restoreCredentials = WalletUtils.loadCredentials(pass,
                            constant.getWalletDirectory() + "/" + fileEntry.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                } catch (CipherException e) {
                    continue;
                }
                this.privateKey = restoreCredentials.getEcKeyPair().getPrivateKey().toString();
                this.walletAddress = restoreCredentials.getAddress();
                System.out.println("private key: "+ privateKey);
                return true;
            }
        }
        return false;
    }

    public String getBalance(String walletAddress){
        if (walletAddress == null){
            return "Load wallet";
        }
        // connect to node
        Web3j web3 = Web3j.build(new HttpService(constant.getNodeAddress()));

        // send asynchronous requests to get balance
        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = web3
                    .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger wei = ethGetBalance.getBalance();


        String data = "Wallet address: " + walletAddress +
                "\nWallet balance: " + wei + "  wei";
        return data;
    }

    public String getBalance(){
        return getBalance(walletAddress);
    }

    /**
     *
     * @param privateKey
     * @param recipientAddress
     * @param amountToBeSent
     * @return
     */
    public boolean transfer(String privateKey, String recipientAddress, String amountToBeSent){
        Web3j web3 = Web3j.build(new HttpService(constant.getNodeAddress()));


        // Decrypt private key into Credential object
        Credentials credentials = Credentials.create(privateKey);

        // Get the latest nonce of current account
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3
                    .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();


        // Value to transfer (in wei)
        BigInteger value = Convert.toWei(amountToBeSent, Convert.Unit.ETHER).toBigInteger();
        // Gas Parameter
        BigInteger gasLimit = BigInteger.valueOf(21000);
        BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();


        // Prepare the rawTransaction
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit,
                recipientAddress, value);

        // Sign the transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        // Send transaction
        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //get TransactionHash
        String transactionHash = ethSendTransaction.getTransactionHash();


        // Wait for transaction to be mined
        Optional<TransactionReceipt> transactionReceipt = null;
        do {
            EthGetTransactionReceipt ethGetTransactionReceiptResp = null;
            try {
                ethGetTransactionReceiptResp = web3.ethGetTransactionReceipt(transactionHash)
                        .send();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            transactionReceipt = ethGetTransactionReceiptResp.getTransactionReceipt();
            try {
                Thread.sleep(3000); // Wait for 3 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } while (!transactionReceipt.isPresent());

        System.out.println("Transaction " + transactionHash + " was mined in block # "
                + transactionReceipt.get().getBlockNumber());
        try {
            System.out.println("Balance: "
                    + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send().getBalance().toString(), Convert.Unit.ETHER));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public boolean transfer(String recipientAddress, String amountToBeSent){
        if (privateKey == null){
            System.out.println("Load wallet");
            return false;
        }
        return transfer(this.privateKey,recipientAddress,amountToBeSent);
    }

    public void history(){
        Web3j web3 = Web3j.build(new HttpService(constant.getNodeAddress()));
        List<EthBlock.TransactionResult> txs = null;
        try {
            txs = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send().getBlock().getTransactions();
        } catch (IOException e) {
            e.printStackTrace();
        }
        txs.forEach(tx -> {
            EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) tx.get();
            System.out.println(new Gson().toJson(transaction));
        });
    }
}
