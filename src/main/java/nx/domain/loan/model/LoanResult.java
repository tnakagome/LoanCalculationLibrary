package nx.domain.loan.model;

/**
 * ローンの最終結果<br>
 * 元金、利息等の総額を個別に集計
 */
public class LoanResult {
    /**
     * 支払った元金の合計
     */
    private long principal;

    /**
     * 支払った利息の合計
     */
    private long interest;

    /**
     * 支払った繰り上げ返済額の合計
     */
    private long prepayment;

    /**
     * 総支払額
     */
    private long total;

    /**
     * 残元金
     */
    private long balance;

    /**
     * 未払い利息が発生した場合、その合計
     */
    private long accruedInterest;

    public LoanResult() {
        principal       = 0;
        interest        = 0;
        prepayment      = 0;
        total           = 0;
        balance         = 0;
        accruedInterest = 0;
    }

    public void addPrincipal(final long principal) {
        this.principal += principal;
        this.total     += principal;
    }

    public void addInterest(final long interest) {
        this.interest += interest;
        this.total    += interest;
    }

    public void addPrepayment(final long prepayment) {
        this.prepayment += prepayment;
        this.total      += prepayment;
    }

    public void setBalance(final long balance) {
        this.balance = balance;
    }

    public void addAccruedInterest(final long accruedInterest) {
        this.accruedInterest += accruedInterest;
    }

    /**
     * @return 支払った元金の合計
     */
    public long getPrincipal() { return principal; }

    /**
     * @return 支払った利息の合計
     */
    public long getInterest() { return interest; }

    /**
     * @return 支払った繰り上げ返済額の合計
     */
    public long getPrepayment() { return prepayment; }

    /**
     * @return 総支払額
     */
    public long getTotal() { return total; }

    /**
     * @return 残元金
     */
    public long getBalance() { return balance; }

    /**
     * @return 未払い利息の合計
     */
    public long getAccruedInterest() { return accruedInterest; }
}
