package nx.domain.loan.payment;

import org.junit.Rule;
import org.junit.rules.TestName;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.PaymentRecord;

/**
 * デバッグ用に償還表を出力
 */
public abstract class TablePrinter {
    @Rule public TestName name = new TestName();

    /**
     * 未払い利息を含まない償還表を出力
     * @param loanInfo
     * @param table
     */
    protected void printTable(final LoanInfo loanInfo, final AbstractPaymentTable table) {
        System.out.format("# %s\n", name.getMethodName());
        if (loanInfo.paymentType == PaymentType.CONSTANT_PAYMENT)
            System.out.println("########## 元利均等償還表 ##########");
        else
            System.out.println("########## 元金均等償還表 ##########");
        for (PaymentRecord r : table) {
            System.out.format("%3d回 金利 %.3f%% 　元本 %,d円 　利息 %,d円 　繰上額 %,d円 　合計 %,d円 　元本残高 %,d円\n",
                    r.getIndex(), r.getRate() * 100, r.getPrincipal(), r.getInterest(),
                    r.getPrepayment(), r.getTotal(), r.getBalance());
        }
    }

    /**
     * 未払い利息も含めて償還表を出力
     * @param loanInfo
     * @param table
     */
    protected void printTableAccrued(final LoanInfo loanInfo, final AbstractPaymentTable table) {
        System.out.format("# %s\n", name.getMethodName());
        if (loanInfo.paymentType == PaymentType.CONSTANT_PAYMENT)
            System.out.println("########## 元利均等償還表 ##########");
        else
            throw new IllegalArgumentException("printTableAccrued is only applicable for constant payment.");

        long accruedTotal = 0;
        for (int i = 0; i < loanInfo.installments; i++) {
            PaymentRecord r = table.get(i);
            accruedTotal += r.getAccruedInterest();
            System.out.format("%3d回 金利 %.3f%% 　元本 %,d円 　利息 %,d円 　繰上額 %,d円 　合計 %,d円 　元本残高 %,d円 　未払い利息 %,d円 　未払い利息累計 %,d円\n",
                    i, r.getRate() * 100, r.getPrincipal(), r.getInterest(),
                    r.getPrepayment(), r.getTotal(), r.getBalance(), r.getAccruedInterest(), accruedTotal);
        }
    }
}
