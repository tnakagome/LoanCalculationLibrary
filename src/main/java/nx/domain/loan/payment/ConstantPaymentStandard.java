package nx.domain.loan.payment;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;
import nx.domain.loan.model.PaymentRecord;

/**
 * 元利均等方式による償還表<br>
 * 5年・125%ルール適用あり<br>
 * Level Paymentとも言われる
 */
public class ConstantPaymentStandard extends AbstractPaymentTable {
    protected final long initialMonthlyPayment;

    public ConstantPaymentStandard(final LoanInfo loanInfo) {
        super(loanInfo);
        initialMonthlyPayment = getMonthlyPayment(0, loanInfo.annualRate, loanInfo.amount);
        if (loanInfo.paymentType != PaymentType.CONSTANT_PAYMENT)
            throw new IllegalArgumentException("bad payment type");
        initialize();
    }

    /**
     * 償還表を初期化
     */
    private void initialize() {
        for (int i = 0; i < loanInfo.installments; i++) {
            table[i] = createRecord(i);
        }
        // 最終回の残元本がマイナスの場合は調整
        PaymentRecord r = table[loanInfo.installments - 1];
        if (r.getBalance() < 0) {
            r.setPrincipal(r.getPrincipal() + r.getBalance());
            r.setTotal(r.getTotal() + r.getBalance());
            r.setBalance(0);
        }
    }

    /**
     * ローン設定当初のn回目の返済情報を作成
     *
     * @param n 返済回。初回は0
     * @return PaymentRecord 返済情報
     */
    private PaymentRecord createRecord(final int n) {
        final PaymentRecord r = new PaymentRecord();
        r.setIndex(n);
        long currentBalance = (n == 0) ? loanInfo.amount : table[n - 1].getBalance();
        r.setRate(loanInfo.annualRate);
        r.setTotal(initialMonthlyPayment);
        r.setInterest(Math.round((double)currentBalance * loanInfo.annualRate / 12.0D));
        r.setPrincipal(initialMonthlyPayment - r.getInterest());
        r.setBalance(currentBalance - r.getPrincipal());
        return r;
    }

    /**
     * 一回の支払額の計算(元金+利息)<br>
     *  償還表作成時や、金利変更や繰り上げ返済を行うときに利用
     *
     * @param n この回以降の返済月額を計算。初回は0。
     * @param rate n回目以降に適用される年利
     * @param balance 前回の元本残額
     * @return 一回の新しい支払額
     */
    protected long getMonthlyPayment(final int n, final double rate, final long balance) {
        final double monthlyRate = rate / 12.0D;
        return Math.round((double)balance * monthlyRate / (1 - (Math.pow(1 + monthlyRate,  -1 * (loanInfo.installments - n)))));
    }

    /**
     * 繰り上げ返済処理
     *
     * @param n n回目に繰り上げ返済を実施
     * @param amount 繰り上げ返済の額
     */
    public void prepayment(final int n, long amount) {
        if (n < 0 || n >= loanInfo.installments)
            throw new IllegalArgumentException("bad argument n: " + n);
        if (amount < 0)
            throw new IllegalArgumentException("bad amount: " + amount);

        /*
         * n回目の再計算
         */
        final PaymentRecord r = table[n];
        if (amount > r.getBalance())
            amount = r.getBalance();
        r.setPrepayment(amount);
        long total = r.getPrincipal() + r.getInterest() + amount;
        r.setTotal(total);
        long balance = (n == 0) ? loanInfo.amount : table[n - 1].getBalance();
        r.setBalance(balance - r.getPrincipal() - amount);

        if (n == loanInfo.installments - 1)
            return;

        /*
         * n+1回目以降の再計算
         */
        if (loanInfo.prepaymentType == PrepaymentType.AMOUNT)
            prepaymentReducePrincipal(n);
        else
            prepaymentShortenDuration(n);
    }

    /**
     * 繰り上げ返済処理(返済額軽減型)<br>
     * 支払月額を計算し直し、最終回まで新しい額で返済する。返済回数は変わらない。
     *
     * @param n 繰り上げ返済を実施した返済回
     */
    private void prepaymentReducePrincipal(int n) {
        PaymentRecord prev = table[n++];
        PaymentRecord r = table[n];
        long newMonthlyAmount = getMonthlyPayment(n, r.getRate(), prev.getBalance());
        for (int i = n; i < loanInfo.installments; i++) {
            prev = table[i - 1];
            r = table[i];
            long interest = Math.round((double)(prev.getBalance()) * r.getRate() / 12.0D);
            r.setInterest(interest);
            long newPrincipal = newMonthlyAmount - interest;
            if (newPrincipal > prev.getBalance())
                newPrincipal = prev.getBalance();
            r.setPrincipal(newPrincipal);
            r.setTotal(r.getPrincipal() + r.getInterest());
            r.setBalance(prev.getBalance() - newPrincipal);
        }
    }

    /**
     * 繰り上げ返済処理(返済期間短縮型)<br>
     * 支払月額は変更せずにそのまま計算を続ける。支払回数が少なくなる。
     * 
     * @param n 繰り上げ返済を実施した返済回
     */
    private void prepaymentShortenDuration(int n) {
        long monthlyPayment = table[n].getPrincipal() + table[n].getInterest();
        for (int i = ++n; i < loanInfo.installments; i++) {
            PaymentRecord prev = table[i - 1];
            PaymentRecord r = table[i];
            long interest = Math.round((double)(prev.getBalance()) * r.getRate() / 12.0D);
            r.setInterest(interest);
            long newPrincipal = monthlyPayment - interest;
            if (newPrincipal > prev.getBalance())
                newPrincipal = prev.getBalance();
            r.setPrincipal(newPrincipal);
            r.setTotal(r.getPrincipal() + r.getInterest());
            r.setBalance(prev.getBalance() - newPrincipal);
        }
    }

    /**
     * 利率変更<br>
     * 利率は即時変更されるが、5年・125%ルールにもとづいて毎月の返済額が変更される。<br>
     * 未払い利息が発生する可能性がある。
     * 
     * @param n 新しい利率を適用する返済回
     * @param newRate 新しい利率
     */
    @Override
    public void changeRate(final int n, final double newRate) {
        if (loanInfo.rateType == RateType.FIXED)
            throw new IllegalArgumentException("Rate is fixed.");

        long monthlyPayment = initialMonthlyPayment;
        for (int i = n; i < loanInfo.installments; i++) {
            long balance = (i == 0) ? loanInfo.amount : table[i - 1].getBalance();
            // 5年に一回返済額を変更
            // 新しい額は前回の1.25倍が上限
            if (i >= 60 && i % 60 == 0) {
                long previousMonthly = monthlyPayment;
                monthlyPayment = getMonthlyPayment(n, newRate, balance);
                if (Math.round((double)previousMonthly * 1.25D) < monthlyPayment)
                    monthlyPayment = Math.round((double)previousMonthly * 1.25D);
                else if (previousMonthly > monthlyPayment)
                    monthlyPayment = previousMonthly;
            }
            PaymentRecord r = table[i];
            r.setRate(newRate);
            long interest = Math.round((double)(balance) * newRate / 12.0D);
            if (interest > monthlyPayment) {
                /* 未払い利息発生 */
                r.setInterest(monthlyPayment);
                r.setPrincipal(0);
                r.setAccruedInterest(interest - monthlyPayment);
                r.setTotal(monthlyPayment);
                r.setBalance(balance);
            }
            else {
                r.setInterest(interest);
                r.setPrincipal(monthlyPayment - interest);
                r.setAccruedInterest(0);
                r.setTotal(monthlyPayment);
                r.setBalance(balance - r.getPrincipal());
            }
        }
    }
}
