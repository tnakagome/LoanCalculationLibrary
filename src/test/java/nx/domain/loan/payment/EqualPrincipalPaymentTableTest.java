package nx.domain.loan.payment;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;

public class EqualPrincipalPaymentTableTest extends TablePrinter {
    LoanInfo loanInfo;
    EqualPrincipalPayment table;

    @Before
    public void prepare() {
        loanInfo = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                    PaymentType.EQUAL_PRINCIPAL_PAYMENT, PrepaymentType.AMOUNT);
        table = new EqualPrincipalPayment(loanInfo);
    }

    @Test
    public void testPrincipal() {
        assertEquals(71429, table.get(0).getPrincipal());
        assertEquals(71249, table.get(419).getPrincipal());
    }

    @Test
    public void testInterest() {
        assertEquals(19375, table.get(0).getInterest());
        assertEquals(46, table.get(419).getInterest());
    }

    @Test
    public void testTotal() {
        assertEquals(90804, table.get(0).getTotal());
        assertEquals(71295, table.get(419).getTotal());
    }

    @Test
    public void testBalance() {
        assertEquals(0, table.get(419).getBalance());
    }

    @Test
    public void testPrepaymentAmount1() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.prepayment(11, 3000000);
        assertEquals(63920, table.get(419).getPrincipal());
        assertEquals(41, table.get(419).getInterest());
    }

    @Test
    public void testPrepaymentAmount2() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.prepayment(23, 3000000);
        assertEquals(63769, table.get(419).getPrincipal());
        assertEquals(41, table.get(419).getInterest());
    }

    @Test
    public void testLargePrepaymentNoThrow() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.prepayment(0, 33000000);
    }

    @Test
    public void testPrepaymentDuration1() {
        LoanInfo loan = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                PaymentType.EQUAL_PRINCIPAL_PAYMENT, PrepaymentType.DURATION);
        EqualPrincipalPayment table = new EqualPrincipalPayment(loan);
        table.prepayment(11, 3000000);
        assertEquals(71267, table.get(377).getPrincipal());
    }

    @Test
    public void testMultiplePrepaymentAmount() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.prepayment(36, 1000000);
        table.prepayment(24, 1000000);
        assertEquals(66259, table.get(419).getPrincipal());
    }

    @Test
    public void testMultiplePrepaymentDuration() {
        LoanInfo loan = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                PaymentType.EQUAL_PRINCIPAL_PAYMENT, PrepaymentType.DURATION);
        EqualPrincipalPayment table = new EqualPrincipalPayment(loan);
        table.prepayment(36, 1000000);
        table.prepayment(24, 1000000);
        assertEquals(71261, table.get(391).getPrincipal());
    }

    @Test
    public void testChangeRate1() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.changeRate(12, 0.03D);
        assertEquals(71429, table.get(12).getPrincipal());
        assertEquals(72857, table.get(12).getInterest());
        assertEquals(144286, table.get(12).getTotal());
    }

    @Test
    public void testMultipleRateChange1() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.changeRate(12, 0.01D);
        table.changeRate(24, 0.02D);
        assertEquals(71,368, table.get(419).getTotal());
        assertEquals(0, table.get(419).getBalance());
    }

    @Test
    public void testMultipleRateChange2() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.changeRate(24, 0.02D);
        table.changeRate(12, 0.01D); // この回以降最終回まで1%で上書きされるので上の行の2%はなかったことになる
        assertEquals(71308, table.get(419).getTotal());
        assertEquals(0, table.get(419).getBalance());
    }

    @Test
    public void testPrepaymentMoreThanBalance() {
        EqualPrincipalPayment table = new EqualPrincipalPayment(loanInfo);
        table.prepayment(417, 200000);
        assertEquals(142678, table.get(417).getPrepayment());
        assertEquals(214245, table.get(417).getTotal());
        assertEquals(0, table.get(418).getPrincipal());
    }
}
