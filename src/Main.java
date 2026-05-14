import service.BankingService;

public class Main {
    public static void main(String[] args) {
        BankingService bankingService = new BankingService();
        bankingService.start();
    }
}
