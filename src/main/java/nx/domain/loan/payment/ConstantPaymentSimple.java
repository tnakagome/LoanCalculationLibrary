package nx.domain.loan.payment;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;
import nx.domain.loan.model.PaymentRecord;

/**
 * 元利均等返済で金利と返済額を随時変更<br>
 * 5年・125%ルール適用なし｡未払い利息は発生しない。一部のネット銀行等が採用。
 */
public class ConstantPaymentSimple extends ConstantPaymentStandard {
    public ConstantPaymentSimple(final LoanInfo loanInfo) {
        super(loanInfo);
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
        if (amount <= 0)
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
        PaymentRecord prev, r;
        long newMonthlyAmount = 0;
        for (int i = n + 1; i < loanInfo.installments; i++) {
            prev = table[i - 1];
            r = table[i];
            if (prev.getPrepayment() > 0)
                newMonthlyAmount = getMonthlyPayment(i, r.getRate(), prev.getBalance());
            long interest = Math.round((double)(prev.getBalance()) * r.getRate() / 12.0D);
            r.setInterest(interest);
            long newPrincipal = newMonthlyAmount - interest;
            if (newPrincipal > prev.getBalance())
                newPrincipal = prev.getBalance();
            r.setPrincipal(newPrincipal);
            r.setTotal(r.getPrincipal() + r.getInterest() + r.getPrepayment());
            r.setBalance(prev.getBalance() - newPrincipal - r.getPrepayment());
        }
    }

    /**
     * 繰り上げ返済処理(返済期間短縮型)<br>
     * 繰り上げた分だけ元金は減らすが支払月額は据え置き。繰り上げ額次第で返済回数が少なくなる。
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
            r.setTotal(r.getPrincipal() + r.getInterest() + r.getPrepayment());
            r.setBalance(prev.getBalance() - newPrincipal - r.getPrepayment());
        }
    }

    /**
     * 利率の変更が毎月の返済額に即時反映される
     * 
     * @param n 新しい利率を適用する返済回
     * @param newRate 新しい利率
     */
    @Override
    public void changeRate(final int n, final double newRate) {
        if (loanInfo.rateType == RateType.FIXED)
            throw new IllegalArgumentException("Rate is fixed.");
        if (n < 0 || n >= loanInfo.installments)
            throw new IllegalArgumentException("bad argument n: " + n);
        if (newRate < 0D)
            throw new IllegalArgumentException("bad rate: " + newRate);

        PaymentRecord r = table[n];
        PaymentRecord prev = table[n - 1];
        long monthlyPayment = getMonthlyPayment(n, newRate, prev.getBalance());
        for (int i = n; i < loanInfo.installments; i++) {
            prev = table[i - 1];
            r = table[i];
            r.setRate(newRate);
            long balance = prev.getBalance();
            long interest = Math.round((double)balance * newRate / 12.0D);
            r.setInterest(interest);
            r.setPrincipal(monthlyPayment - interest);
            r.setTotal(monthlyPayment + r.getPrepayment());
            r.setBalance(balance - r.getPrincipal() - r.getPrepayment());
        }
    }
}
