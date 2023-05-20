package nx.domain.loan.payment;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.PaymentRecord;
import nx.domain.loan.model.LoanInfo.RateType;

/**
 * 元利均等返済で金利と返済額を随時変更<br>
 * 5年・125%ルール適用なし｡一部のネット銀行等が採用
 */
public class ConstantPaymentSimple extends ConstantPaymentStandard {
    public ConstantPaymentSimple(final LoanInfo loanInfo) {
        super(loanInfo);
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
