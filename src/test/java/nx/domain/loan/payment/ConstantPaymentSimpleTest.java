package nx.domain.loan.payment;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import nx.domain.loan.model.LoanInfo;
import nx.domain.loan.model.LoanInfo.PaymentType;
import nx.domain.loan.model.LoanInfo.PrepaymentType;
import nx.domain.loan.model.LoanInfo.RateType;

public class ConstantPaymentSimpleTest extends TablePrinter {
    LoanInfo loanInfo;
    ConstantPaymentSimple table;
    
    @Before
    public void prepare() {
        loanInfo = new LoanInfo(30000000, 35, 0, 0.00775, RateType.VARIABLE,
                PaymentType.CONSTANT_PAYMENT, PrepaymentType.DURATION);
        table = new ConstantPaymentSimple(loanInfo);
    }

    @Test
    public void testPrepaymentAndChangeRate1() {
        table.changeRate(12, 0.01D);
        table.changeRate(24, 0.02D);
        table.prepayment(60, 1000000);
        assertEquals(84600, table.get(12).getTotal());
        assertEquals(78465, table.get(400).getBalance());
    }
}
