package nx.domain.loan.payment;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;
import nx.domain.loan.model.PaymentRecord;

/**
 * 元金均等方式による償還表
 * <br>
 * 英語の別名はConstant Amortization Mortgage (CAM) Loan
 */
public class EqualPrincipalPayment extends AbstractPaymentTable {
    private final long initialPrincipal;

    public EqualPrincipalPayment(final LoanInfo loanInfo) {
        super(loanInfo);
        if (loanInfo.paymentType != PaymentType.EQUAL_PRINCIPAL_PAYMENT)
            throw new IllegalArgumentException("bad payment type");
        initialPrincipal = Math.round((double)loanInfo.amount / (double)loanInfo.installments);
        initialize();
    }

    /**
     * 償還表を初期化
     */
    public void initialize() {
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
     * n回目の返済情報を作成
     * 
     * @param n 返済回。初回は0
     * @return PaymentRecord 返済情報
     */
    private PaymentRecord createRecord(final int n) {
        PaymentRecord r = new PaymentRecord();
        r.setIndex(n);
        r.setRate(loanInfo.annualRate);
        r.setPrincipal(initialPrincipal);
        long balance, interest;
        if (n == 0) {
            balance = loanInfo.amount - initialPrincipal;
            interest = Math.round((double)loanInfo.amount * loanInfo.annualRate / 12.0D);
        }
        else {
            balance = table[n - 1].getBalance() - initialPrincipal;
            interest = Math.round((double)(table[n - 1].getBalance()) * loanInfo.annualRate / 12.0D);
        }
        r.setBalance(balance);
        r.setInterest(interest);
        r.setTotal(initialPrincipal + interest);
        return r;
    }

    public void prepayment(final int n, long amount) {
        if (n < 0 || n >= loanInfo.installments)
            throw new IllegalArgumentException("bad argument n: " + n);
        if (amount < 0)
            throw new IllegalArgumentException("bad amount: " + amount);

        /*
         * n回目の再計算
         */
        PaymentRecord r = table[n];
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
     * 元金の月額を計算し直し、最終回まで新しい額で返済する。返済回数は変わらない。
     *
     * @param n 繰り上げ返済を実施した返済回
     */
    private void prepaymentReducePrincipal(final int n) {
        long newPrincipal = Math.round((double)(table[n].getBalance()) / (loanInfo.installments - (n + 1)));
        for (int i = n + 1; i < loanInfo.installments; i++) {
            PaymentRecord prev = table[i - 1];
            PaymentRecord r = table[i];
            if (newPrincipal > prev.getBalance())
                newPrincipal = prev.getBalance();
            r.setPrincipal(newPrincipal);
            final long interest = Math.round((double)(table[i - 1].getBalance()) * (r.getRate() / 12.0D));
            r.setInterest(interest);
            r.setTotal(newPrincipal + interest);
            r.setBalance(table[i - 1].getBalance() - newPrincipal);
        }
    }

    /**
     * 繰り上げ返済処理(返済期間短縮型)<br>
     * 元金は減らすが支払月額は変更しない。繰り上げ額次第で返済回数が少なくなる。
     * 
     * @param n 繰り上げ返済を実施した返済回
     */
    private void prepaymentShortenDuration(final int n) {
        for (int i = n + 1; i < loanInfo.installments; i++) {
            PaymentRecord prev = table[i - 1];
            PaymentRecord r = table[i];
            if (prev.getBalance() < r.getPrincipal())
                r.setPrincipal(prev.getBalance());
            long interest = Math.round((double)(prev.getBalance()) * r.getRate() / 12.0D);
            r.setInterest(interest);
            r.setTotal(r.getPrincipal() + interest);
            r.setBalance(prev.getBalance() - r.getPrincipal());
        }
    }

    @Override
    public void changeRate(final int n, final double newRate) {
        if (loanInfo.rateType == RateType.FIXED)
            throw new IllegalArgumentException("Rate is fixed.");

        for (int i = n; i < loanInfo.installments; i++) {
            PaymentRecord r = table[i];
            long balance = (i == 0) ? loanInfo.amount : table[i - 1].getBalance();
            r.setRate(newRate);
            long interest = Math.round((double)(balance) * newRate / 12.0D);
            r.setInterest(interest);
            r.setTotal(r.getPrincipal() + interest);
        }
        
    }
}
