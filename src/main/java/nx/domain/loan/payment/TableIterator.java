package nx.domain.loan.payment;

import java.util.Iterator;

import nx.domain.loan.model.PaymentRecord;

public class TableIterator implements Iterator<PaymentRecord> {
    private final AbstractPaymentTable table;
    private int position;

    public TableIterator(AbstractPaymentTable table) {
        this.table = table;
    }

    @Override
    public boolean hasNext() {
        return (position < table.size());
    }

    @Override
    public PaymentRecord next() {
        return table.get(position++);
    }
}
