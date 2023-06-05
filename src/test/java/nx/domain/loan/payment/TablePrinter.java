package nx.domain.loan.payment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Rule;
import org.junit.rules.TestName;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanResult;
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
            System.out.format("%3d回 金利 %.3f%% 　元金 %,d円 　利息 %,d円 　繰上額 %,d円 　合計 %,d円 　元金残高 %,d円\n",
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

        for (int i = 0; i < loanInfo.installments; i++) {
            PaymentRecord r = table.get(i);
            System.out.format("%3d回 金利 %.3f%%　　元金 %,d円　　利息 %,d円　　充当未払い利息 %,d円　　繰上額 %,d円　　支払額合計 %,d円　　元金残高 %,d円　　発生未払い利息 %,d円　　未払い利息残高 %,d円\n",
                    i, r.getRate() * 100, r.getPrincipal(), r.getInterest(), r.getAccruedInterestPaid(),
                    r.getPrepayment(), r.getTotal(), r.getBalance(), r.getAccruedInterestNew(), r.getAccruedInterestBalance());
        }
    }

    protected void printLoanResult(final LoanResult result) {
        System.out.format("元金合計 %,d円　　利息合計 %,d円　　充当未払い利息合計 %,d円　　繰上額 %,d円　　総支払額 %,d円　　元金残高 %,d円　　未払い利息残高 %,d円\n",
                result.getPrincipal(), result.getInterest(), result.getAccruedInterestPaid(), result.getPrepayment(),
                result.getTotal(), result.getBalance(), result.getAccruedInterestBalance());
    }

    protected void printTSV(final LoanInfo loanInfo, final AbstractPaymentTable table) {
        writeTSVtoFile(System.out, loanInfo, table);
    }

    protected void writeTSV(final LoanInfo loanInfo, final AbstractPaymentTable table) {
        try {
            File file = new File(name.getMethodName() + ".tsv");
            writeTSVtoFile(new PrintStream(new FileOutputStream(file)), loanInfo, table);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeTSVtoFile(final PrintStream out, final LoanInfo loanInfo, final AbstractPaymentTable table) {
        // 借入条件
        out.println("\t\t借入額\t金利\t金利種別\t返済回数");
        out.format("\t\t%,d\t%.3f%%\t%s\t%d\n\n",
                loanInfo.amount, loanInfo.annualRate * 100, loanInfo.paymentType.toString(), loanInfo.installments);

        // 最終結果
        LoanResult result = table.getResult();
        out.println("\t\t元金計\t利息計\t充当未払い利息計\t繰上額計\t返済額計\t元金残高\t\t未払い利息残高");
        out.format("\t\t%d\t%d\t%d\t%d\t%d\t%d\t\t%d\n\n",
                result.getPrincipal(), result.getInterest(), result.getAccruedInterestPaid(), result.getPrepayment(),
                result.getTotal(), result.getBalance(), result.getAccruedInterestBalance());

        // 償還表
        out.println("返済回\t金利\t元金\t利息\t充当未払い利息\t繰上額\t支払額合計\t元金残高\t発生未払い利息\t未払い利息残高");
        for (int i = 0; i < loanInfo.installments; i++) {
            PaymentRecord r = table.get(i);
            out.format("%d\t%.3f%%\t%,d\t%,d\t%,d\t%,d\t%,d\t%,d\t%,d\t%,d\n",
                    i+1, r.getRate() * 100, r.getPrincipal(), r.getInterest(), r.getAccruedInterestPaid(),
                    r.getPrepayment(), r.getTotal(), r.getBalance(), r.getAccruedInterestNew(), r.getAccruedInterestBalance());
        }
    }
}
