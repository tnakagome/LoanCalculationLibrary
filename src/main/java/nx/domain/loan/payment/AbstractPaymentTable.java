package nx.domain.loan.payment;

import java.util.Iterator;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanResult;
import nx.domain.loan.model.PaymentRecord;

/**
 * 償還表計算の基底クラス
 */
public abstract class AbstractPaymentTable implements Iterable<PaymentRecord> {
    protected final LoanInfo loanInfo;
    protected final PaymentRecord table[];

    protected AbstractPaymentTable(final LoanInfo loanInfo) {
        this.loanInfo = loanInfo;
        this.table = new PaymentRecord[loanInfo.installments];
    }

    /**
     * 償還表作成後に特定の回の返済情報を取得
     * @param n 返済回
     * @return PaymentRecord n回目の返済情報
     */
    public PaymentRecord get(final int n) {
        if (n < 0 || n >= loanInfo.installments)
            throw new IndexOutOfBoundsException();
        return table[n];
    }

    /**
     * 償還表の大きさ(返済回数)を取得
     */
    public int size() {
        return loanInfo.installments;
    }

    /**
     * 　この償還表のIteratorを取得
     */
    public Iterator<PaymentRecord> iterator() {
        return new TableIterator(this);
    }

    /**
     * 償還表から結果を集計
     *
     * @see nx.domain.loan.model.LoanResult
     * @return LoanResultオブジェクト
     */
    public LoanResult getResult() {
        LoanResult result = new LoanResult();
        for (PaymentRecord r : table) {
            if (r == null) {
                throw new IllegalStateException("PaymentRecord is null.");
            }
            result.addPrincipal(r.getPrincipal());
            result.addInterest(r.getInterest());
            result.addPrepayment(r.getPrepayment());
            result.addAccruedInterest(r.getAccruedInterest());
        }
        result.setBalance(table[loanInfo.installments - 1].getBalance());
        return result;
    }

    /**
     * 繰り上げ返済処理
     *
     * @param n n回目に繰り上げ返済を実施
     * @param amount n回目の繰り上げ返済の額
     */
    public abstract void prepayment(int n, long amount);

    /**
     * 利率の変更処理
     *
     * @param n n回目以降の利率を変更
     * @param newRate 新しい利率
     */
    public abstract void changeRate(int n, double newRate);
}
