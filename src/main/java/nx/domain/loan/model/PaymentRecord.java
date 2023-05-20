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
     * 今回の支払い後の残元本
     */
    private long balance;

    /**
     * 今回の繰り上げ返済額
     */
    private long prepayment;

    /**
     * 今回の未払い利息
     */
    private long accruedInterest;

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
     * @param rate 利率
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    /**
     * @return 今回支払う元金
     */
    public long getPrincipal() {
        return principal;
    }

    /**
     * 今回の元金を設定
     *
     * @param principal 元金
     */
    public void setPrincipal(long principal) {
        this.principal = principal;
    }

    /**
     * @return 今回支払う利息
     */
    public long getInterest() {
        return interest;
    }

    /**
     * 今回の利息を設定
     *
     * @param interest 利息
     */
    public void setInterest(long interest) {
        this.interest = interest;
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
     * @param total 支払額合計
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * @return 今回の支払い後の残額
     */
    public long getBalance() {
        return balance;
    }

   /**
    * 今回元金、利息等を支払った後に残る元金の額を設定
    *
    * @param balance 残額
    */
    public void setBalance(long balance) {
        this.balance = balance;
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
     * @param prepayment 繰り上げ返済額
     */
    public void setPrepayment(long prepayment) {
        this.prepayment = prepayment;
    }

    /**
     * @return 今回の未払い利息
     */
    public long getAccruedInterest() {
        return accruedInterest;
    }

    /**
     * 今回の未払い利息を設定
     *
     * @param accruedInterest 未払い利息
     */
    public void setAccruedInterest(long accruedInterest) {
        this.accruedInterest = accruedInterest;
    }
}
