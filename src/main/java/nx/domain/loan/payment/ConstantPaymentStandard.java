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
    protected final int  RATE_CHANGE_INTERVAL = 60;
    protected final long initialMonthlyPayment;

    public ConstantPaymentStandard(final LoanInfo loanInfo) {
        super(loanInfo);
        if (loanInfo.paymentType != PaymentType.CONSTANT_PAYMENT)
            throw new IllegalArgumentException("bad payment type");
        initialMonthlyPayment = getMonthlyPayment(0, loanInfo.annualRate, loanInfo.amount);
        initialize();
    }

    /**
     * 償還表を初期化
     */
    private void initialize() {
        for (int i = 0; i < loanInfo.installments; i++) {
            table[i] = createRecord(i);
        }
        // 最終回の残元金が0でない場合は0になるように調整
        PaymentRecord r = table[loanInfo.installments - 1];
        if (r.getBalance() != 0) {
            r.setPrincipal(r.getPrincipal() + r.getBalance());
            r.setTotal(r.getTotal() + r.getBalance());
            r.setBalance(0);
        }
    }

    /**
     * ローン設定当初のn回目の返済情報を作成
     *
     * @param n 返済回。初回は0
     * @return PaymentRecord n回目の返済情報
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
     * @param balance 前回の元金残額
     * @return 一回の新しい支払額
     */
    protected long getMonthlyPayment(final int n, final double rate, final long balance) {
        final double monthlyRate = rate / 12.0D;
        return Math.round((double)balance * monthlyRate / (1.0D - (Math.pow(1.0D + monthlyRate,  -1.0D * (loanInfo.installments - n)))));
    }

    /**
     * 繰り上げ返済処理
     *
     * @param n 繰り上げ返済を実施する返済回
     * @param amount 繰り上げ返済の額
     */
    @Override
    public void prepayment(final int n, long amount) {
        if (n < 0 || n >= loanInfo.installments)
            throw new IllegalArgumentException("bad argument n: " + n);
        if (amount <= 0)
            throw new IllegalArgumentException("bad amount: " + amount);

        table[n].setPrepayment(amount);
        calculate(0);
    }

    /**
     * 利率変更<br>
     * 指定回以降最終回までの利率を設定する
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

        for (int i = n; i < table.length; i++) {
            table[i].setRate(newRate);
        }
        calculate(0);
    }

    /**
     * 繰上返済時や利率変更時に償還表を再計算
     * 
     * @param start この回から最終回まで再計算する
     */
    protected void calculate(final int start) {
        long accruedInterestBalance = 0;
        boolean updateMonthlyPayment = false;
        long balance, monthlyPayment;
        double currentRate;
        boolean rateChange;
        int lastRateChange = start;

        if (start == 0) {
            balance = loanInfo.amount;
            currentRate = loanInfo.annualRate;
            accruedInterestBalance = 0;
            rateChange = false;
            monthlyPayment = initialMonthlyPayment;
        }
        else {
            PaymentRecord r = table[start - 1];
            balance = r.getBalance();
            currentRate = r.getRate();
            accruedInterestBalance = r.getAccruedInterestBalance();
            rateChange = true;
            monthlyPayment = r.getPrincipal() + r.getInterest() + r.getAccruedInterestPaid();
        }

        for (int i = start; i < table.length; i++) {
            PaymentRecord r = table[i];
            r.reset();
            r.setBalance(balance);

            if (currentRate != r.getRate()) {
                rateChange = true;
            }

            // 5年125%ルール
            if ((rateChange || accruedInterestBalance > 0) && i >= RATE_CHANGE_INTERVAL && i % RATE_CHANGE_INTERVAL == 0) {
                long previousMonthly = monthlyPayment;
                monthlyPayment = getMonthlyPayment(r.getIndex(), r.getRate(), balance);
                if (Math.round((double)previousMonthly * 1.25D) < monthlyPayment)
                    monthlyPayment = Math.round((double)previousMonthly * 1.25D);
                if (Math.abs(previousMonthly - monthlyPayment) * (loanInfo.installments - r.getIndex()) < loanInfo.installments)
                    // 前回の月額と新しい月額の差がわずかな場合は前回の月額を引き続き使用
                    monthlyPayment = previousMonthly;
                updateMonthlyPayment = false;
                rateChange = false;
                currentRate = r.getRate();
                lastRateChange = i;
            }

            // 前回の繰上返済により今回以降の返済月額が変更になる場合
            if (updateMonthlyPayment && accruedInterestBalance == 0) {
                monthlyPayment = getMonthlyPayment(r.getIndex(), r.getRate(), balance);
                updateMonthlyPayment = false;
            }

            // 繰上返済
            if (r.getPrepayment() > 0) {
                prepayment(r, accruedInterestBalance);
                if (loanInfo.prepaymentType == PrepaymentType.AMOUNT) {
                    // 次回以降の月額を変更
                    updateMonthlyPayment = true;
                }
                accruedInterestBalance = r.getAccruedInterestBalance();
            }

            long interest = Math.round((double)balance * r.getRate() / 12.0D);
            long thisMonthPayment = monthlyPayment;

            // 未払い利息処理
            if (accruedInterestBalance > 0) {
                thisMonthPayment = accruedInterest(r, thisMonthPayment, accruedInterestBalance);
                accruedInterestBalance = r.getAccruedInterestBalance();
            }

            // 元金と利息の処理
            payment(r, thisMonthPayment, interest);

            balance = r.getBalance();
            accruedInterestBalance = r.getAccruedInterestBalance();
        }

        // 最終回の残債処理
        PaymentRecord last = table[table.length - 1];
        if (last.getBalance() > 0) {
            // 最終回に元金が残っている場合
            if (last.getBalance() < loanInfo.installments) {
                // 残債が少額の場合は最終回の支払元金に加えて残債を0にする
                last.setPrincipal(last.getPrincipal() + last.getBalance());
                last.setBalance(0);
            }
            else if (lastRateChange + RATE_CHANGE_INTERVAL < loanInfo.installments) {
                // 遡って支払月額を増やすことが可能なら、支払月額を増やして途中から再計算する
                calculate(lastRateChange + RATE_CHANGE_INTERVAL);
            }
        }
    }

    /**
     * 繰上返済処理
     *
     * @param r 処理対象月の支払情報
     * @param accruedInterestBalance 未払い利息の累計
     */
    private void prepayment(final PaymentRecord r, final long accruedInterestBalance) {
        long prepayment = r.getPrepayment();
        long used = 0;

        // 未払い利息が積みあがっている場合は最優先で充当
        if (accruedInterestBalance > 0) {
            if (accruedInterestBalance > prepayment) {
                r.setAccruedInterestPaid(prepayment);
                r.setAccruedInterestBalance(accruedInterestBalance - prepayment);
                used = prepayment;
                prepayment = 0;
            }
            else {
                r.setAccruedInterestPaid(accruedInterestBalance);
                r.setAccruedInterestBalance(0);
                used = accruedInterestBalance;
                prepayment -= accruedInterestBalance;
            }
        }

        // 繰上返済原資が残っていれば元金に充当
        if (prepayment > 0) {
            long balance = r.getBalance();
            if (prepayment >= balance) {
                // 残元金より繰上返済原資のほうが多い場合は残元金は0になる
                r.setBalance(0);
                // 繰上返済額は使った分だけに再設定
                r.setPrepayment(used + balance);
            }
            else {
                // 残元金のほうが多い場合は元金を減額
                r.setBalance(r.getBalance() - prepayment);
            }
        }
    }

    /**
     * 未払い利息の処理
     *
     * @param r 処理対象月の支払情報
     * @param amount 支払い可能額
     * @param accruedInterestBalance これまでの未払い利息残高
     * @return 未払い利息を処理した後で利息や元金に充当できる残額
     */
    private long accruedInterest(final PaymentRecord r, long amount, final long accruedInterestBalance) {
        if (accruedInterestBalance >= amount) {
            r.setAccruedInterestPaid(amount);
            r.setAccruedInterestBalance(accruedInterestBalance - amount);
            return 0;
        }
        else {
            long remaining = amount - accruedInterestBalance;
            r.setAccruedInterestPaid(accruedInterestBalance);
            r.setAccruedInterestBalance(0);
            return remaining;
        }
    }

    /**
     * 元金と利息の処理
     *
     * @param r 処理対象月の支払情報
     * @param amount 支払い可能額
     * @param interest 今回の利息額
     */
    private void payment(final PaymentRecord r, long amount, final long interest) {
        if (interest > amount) {
            // 未払い利息発生
            r.setInterest(amount);
            r.setPrincipal(0);
            r.setAccruedInterestNew(interest - amount);
            r.setAccruedInterestBalance(r.getAccruedInterestBalance() + r.getAccruedInterestNew());
            amount = 0;
        }
        else {
            r.setInterest(interest);
            r.setAccruedInterestNew(0);
            amount -= interest;
            if (r.getBalance() < amount) {
                r.setPrincipal(r.getBalance());
                r.setBalance(0);
            }
            else {
                r.setPrincipal(amount);
                r.setBalance(r.getBalance() - amount);
            }
        }
        long total = r.getInterest() + r.getPrincipal() + r.getPrepayment() + r.getAccruedInterestPaid();
        r.setTotal(total);
    }
}
