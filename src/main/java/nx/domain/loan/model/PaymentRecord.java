package nx.domain.loan.model;

/**
 * 各回のローン情報
 */
public class PaymentRecord {
    /**
     * 返済回(初回は0)
     */
    private int index;

    /**
     * 今回の年利
     */
    private double rate;

    /**
     * 今回支払う元金
     */
    private long principal;

    /**
     * 今回支払う利息
     */

    private long interest;

    /**
     * 今回の支払額合計 (元金+利息+繰り上げ返済額)
     */
    private long total;

    /**
     * 今回の支払い後の残元金
     */
    private long balance;

    /**
     * 今回の繰り上げ返済額
     */
    private long prepayment;

    /**
     * 今回新たに発生した未払い利息
     */
    private long accruedInterestNew;

    /**
     * 前回までの未払い利息のうち今回返済した額
     */
    private long accruedInterestPaid;

    /**
     * 未払い利息累計
     */
    private long accruedInterestBalance;

    /**
     * 返済回
     */
    public int getIndex() {
        return index;
    }

    /**
     * 返済回を設定
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return 今回の年利
     */
    public double getRate() {
        return rate;
    }

    /**
     * 今回の年利を設定
     *
     * @param rate 利率(年利)
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    /**
     * @return 今回支払う元金額
     */
    public long getPrincipal() {
        return principal;
    }

    /**
     * 今回の元金を設定
     *
     * @param amount 元金額
     */
    public void setPrincipal(long amount) {
        this.principal = amount;
    }

    /**
     * @return 今回支払う利息額
     */
    public long getInterest() {
        return interest;
    }

    /**
     * 今回の利息を設定
     *
     * @param amount 利息額
     */
    public void setInterest(long amount) {
        this.interest = amount;
    }

    /**
     * @return 今回の支払額合計
     */
    public long getTotal() {
        return total;
    }

    /**
     * 今回の支払額合計を設定
     *
     * @param amount 支払額合計
     */
    public void setTotal(long amount) {
        this.total = amount;
    }

    /**
     * @return 今回の支払い後の元金残額
     */
    public long getBalance() {
        return balance;
    }

   /**
    * 今回元金、利息等を支払った後に残る元金の額を設定
    *
    * @param amount 元金残額
    */
    public void setBalance(long amount) {
        this.balance = amount;
    }

    /**
     * @return 今回の繰り上げ返済額
     */
    public long getPrepayment() {
        return prepayment;
    }

    /**
     * 今回の繰り上げ返済額を設定
     *
     * @param amount 繰り上げ返済額
     */
    public void setPrepayment(long amount) {
        this.prepayment = amount;
    }

    /**
     * @return 今回新たに発生した未払い利息額
     */
    public long getAccruedInterestNew() {
        return accruedInterestNew;
    }

    /**
     * 今回新たに発生した未払い利息を設定
     *
     * @param amount 未払い利息額
     */
    public void setAccruedInterestNew(long amount) {
        this.accruedInterestNew = amount;
    }

    /**
     * @return 今回返済した未払い利息額
     */
    public long getAccruedInterestPaid() {
        return accruedInterestPaid;
    }

    /**
     * 今回返済した未払い利息を設定
     *
     * @param amount 未払い利息額
     */
    public void setAccruedInterestPaid(long amount) {
        this.accruedInterestPaid = amount;
    }

    /**
     * @return 未払い利息累計
     */
    public long getAccruedInterestBalance() {
        return accruedInterestBalance;
    }

    /**
     * 今回の未払い利息累計を設定
     *
     * @param amount 未払い利息累計
     */
    public void setAccruedInterestBalance(long amount) {
        this.accruedInterestBalance = amount;
    }

    /**
     * 繰上額と利率以外の項目を初期化
     */
    public void reset() {
        accruedInterestNew = 0;
        accruedInterestPaid = 0;
        accruedInterestBalance = 0;
        balance = 0;
        interest = 0;
        principal = 0;
        total = 0;
    }
}
