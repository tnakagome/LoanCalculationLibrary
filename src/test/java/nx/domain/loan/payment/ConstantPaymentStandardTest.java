package nx.domain.loan.payment;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;

public class ConstantPaymentStandardTest extends TablePrinter {
    LoanInfo loanInfo;
    ConstantPaymentStandard table;

    @Before
    public void prepare() {
        loanInfo = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                    PaymentType.CONSTANT_PAYMENT, PrepaymentType.AMOUNT);
        table = new ConstantPaymentStandard(loanInfo);
    }

    @Test
    public void testPrincipal() {
        assertEquals(62201, table.get(0).getPrincipal());
        assertEquals(81523, table.get(419).getPrincipal());
    }

    @Test
    public void testInterest() {
        assertEquals(19375, table.get(0).getInterest());
        assertEquals(53, table.get(419).getInterest());
    }

    @Test
    public void testTotal() {
        assertEquals(81576, table.get(0).getTotal());
        assertEquals(81576, table.get(419).getTotal());
    }

    @Test
    public void testBalance() {
        assertEquals(197, table.get(419).getBalance());
    }

    @Test
    public void testPrepaymentAmount1() {
        ConstantPaymentStandard table = new ConstantPaymentStandard(loanInfo);
        table.prepayment(11, 3000000);
        assertEquals(73107, table.get(419).getPrincipal());
        assertEquals(47, table.get(419).getInterest());
    }

    @Test
    public void testPrepaymentDuration1() {
        LoanInfo loanInfo = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                PaymentType.CONSTANT_PAYMENT, PrepaymentType.DURATION);
        ConstantPaymentStandard table = new ConstantPaymentStandard(loanInfo);
        table.prepayment(11, 3000000);
        assertEquals(69580, table.get(372).getPrincipal());
        assertEquals(45, table.get(372).getInterest());
    }
    
    @Test
    public void testMultiplePrepaymentAmount() {
    	ConstantPaymentStandard table = new ConstantPaymentStandard(loanInfo);
        table.prepayment(36, 1000000);
        table.prepayment(24, 1000000);
        assertEquals(75759, table.get(419).getTotal());
        assertEquals(150, table.get(419).getBalance());
    }

    @Test
    public void testMultiplePrepaymentDuration() {
        LoanInfo loan = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                PaymentType.CONSTANT_PAYMENT, PrepaymentType.DURATION);
        ConstantPaymentStandard table = new ConstantPaymentStandard(loan);
        table.prepayment(36, 1000000);
        table.prepayment(24, 1000000);
        assertEquals(64564, table.get(388).getPrincipal());
        assertEquals(0, table.get(388).getBalance());
    }

    @Test
    public void testChangeRateWithAccuredInterest1() {
        LoanInfo loanInfo = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                PaymentType.CONSTANT_PAYMENT, PrepaymentType.DURATION);
        ConstantPaymentStandard table = new ConstantPaymentStandard(loanInfo);
        table.changeRate(12, 0.04);
        assertEquals(15927, table.get(59).getAccruedInterest());
        assertEquals(164758, table.get(419).getTotal());
        assertEquals(0, table.get(419).getBalance());
    }

    @Test
    public void testMultipleRateChange1() {
        ConstantPaymentStandard table = new ConstantPaymentStandard(loanInfo);
    	table.changeRate(24, 0.01);
    	table.changeRate(36, 0.02);
    	assertEquals(99550, table.get(419).getTotal());
    	assertEquals(0, table.get(419).getBalance());
    }

    @Test
    public void testPrepaymentMoreThanBalance() {
    	ConstantPaymentStandard table = new ConstantPaymentStandard(loanInfo);
        table.prepayment(417, 200000);
        assertEquals(163191, table.get(417).getPrepayment());
        assertEquals(244767, table.get(417).getTotal());
        assertEquals(0, table.get(417).getBalance());
        assertEquals(0, table.get(418).getPrincipal());
    }
}
